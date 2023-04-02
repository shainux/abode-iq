package io.sh.abodeiq.plugins.mongo

import com.mongodb.ConnectionString
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.sh.abodeiq.model.*
import io.sh.abodeiq.plugins.mongo.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.InputStream
import java.util.*

const val mongoConfigName = "mongo"
const val cfgUrlName = "url"
const val cfgDbName = "database"
const val atlasDomain = ".mongodb.net"
const val cfgUserName = "username"
const val cfgPassName = "password"

class Mongo(cfg: ConnectionString, dbName: String) : DaoFacade {
    class Configuration {
        var cfg: ApplicationConfig? = null
    }

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, Mongo> {
        var instance: Mongo? = null

        override val key = AttributeKey<Mongo>("MongoPlugin")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Mongo {
            val env =
                pipeline.environment ?: throw java.lang.IllegalStateException("Cannot get application environment")

            env.log.info("Registering Mongo Plugin...")
            val mongoConfig = Configuration().apply(configure).cfg ?: throw java.lang.IllegalArgumentException("Invalid Mongo config")
            val dbName = mongoConfig.tryGetString(cfgDbName)
                ?: throw ApplicationConfigurationException("Invalid Mongo config: no DB name provided")
            instance = instance ?: Mongo(getConnectionString(mongoConfig), dbName)
            runBlocking { instance!!.checkStaticCollections() }
            return instance!!
        }

        private fun getConnectionString(mongoCfg: ApplicationConfig): ConnectionString {
            val embeddedMongoPort = System.getProperty("embeddedMongoPort")
            val connectionStr = if (!embeddedMongoPort.isNullOrBlank()) {   // we're in tests
                "mongodb://localhost:$embeddedMongoPort"
            } else {
                mongoCfg.tryGetString(cfgUrlName)?.let { url ->
                    if (url.contains(atlasDomain)) {
                        val user = mongoCfg.tryGetString(cfgUserName)
                        val pass = mongoCfg.tryGetString(cfgPassName)
                        "mongodb+srv://$user:$pass@$url/?authSource=admin&retryWrites=true&w=majority"
                    } else {
                        url
                    }
                } ?: throw ApplicationConfigurationException("Invalid Mongo config: no url provided")
            }
            return ConnectionString(connectionStr)
        }
    }

    private val client = KMongo.createClient(cfg)
    private val database = client.getDatabase(dbName).coroutine

    private var networkCollection = database.getCollection<NetworkDao>(networkCollectionName)
    private val nodeCollection = database.getCollection<NodeDao>(nodeCollectionName)
    private val readingCollection = database.getCollection<ReadingDao>(readingCollectionName)
    private val messageCollection = database.getCollection<MessageDao>(messageCollectionName)
    private var deviceSpecCollection = database.getCollection<DeviceSpecsDao>(deviceSpecCollectionName)
    private var manufacturerCollection = database.getCollection<ManufacturerDao>(manufacturerCollectionName)
    private var deviceClassCollection = database.getCollection<DeviceClassDao>(deviceClassCollectionName)
    private var recentLogsCollection = database.getCollection<LogRecordDao>(recentLogsCollectionName)

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun checkStaticCollections(){
        if(isCollectionEmpty(manufacturerCollectionName)){
            populateIndex(manufacturersDefinitions) { insertManufacturers(Json.decodeFromStream(it)) }
        }
        if(isCollectionEmpty(deviceClassCollectionName)){
            populateIndex(deviceClassDefinitions) { insertDeviceClasses(Json.decodeFromStream(it)) }
        }
        if(isCollectionEmpty(deviceSpecCollectionName)){
            populateIndex(deviceSpecsDefinitions) { insertDeviceSpecifications(Json.decodeFromStream(it)) }
        }
    }

    private fun populateIndex(resourceName: String, uploadFun: suspend (InputStream) -> Unit){
        val stream = this::class.java.getResource(resourceName)?.openStream()
            ?: throw java.lang.IllegalArgumentException("Cannot find file with static data: $resourceName")
        runBlocking { uploadFun(stream) }
    }

    override suspend fun getNetwork(networkId: String): Network? =
        networkCollection.findOne(NetworkDao::id eq networkId)?.getNetwork()

    override suspend fun getNetworks(): Flow<Network> {
        val latestActivities = nodeCollection.aggregate<NetworkLatestActivity>(
            "[{\$group:{'_id' : '\$networkId', '_latestActivity' : {\$max: '\$lastActivityTs'}}}]"
        ).toList()
            .associate { it._id to it._latestActivity }

        return networkCollection.find().toFlow()
            .map { nw ->
                val dto = nw.getNetwork()
                dto.lastActivity = latestActivities[dto.id]?.let{Date(it)}
                println(dto)
                dto
            }
    }

    override fun cleanNetworks(): Unit = runBlocking {
       networkCollection.drop()
    }

    override fun saveNetwork(nw: Network): Unit = runBlocking {
        networkCollection.save(NetworkDao.of(nw))
    }

    override fun deleteNetwork(networkId: String): Boolean = runBlocking {
        networkCollection.deleteOne(NetworkDao::id eq networkId).deletedCount > 0
    }

    override suspend fun getNode(networkId: String, nodeId: Int): Node? =
        nodeCollection.findOne(
            and(
                NodeDao::networkId eq networkId,
                NodeDao::nodeId eq nodeId
            )
        )?.getNode()

    override suspend fun saveNode(node: Node): Boolean {
        nodeCollection.findOneAndDelete(
            and(
                NodeDao::networkId eq node.networkId,
                NodeDao::nodeId eq node.nodeId
            )
        )
        return nodeCollection.save(NodeDao.of(node))?.upsertedId != null
    }

    suspend fun deleteNode(networkId: String, nodeId: Int): Boolean =
        nodeCollection.deleteOne(
            and(
                NodeDao::networkId eq networkId,
                NodeDao::nodeId eq nodeId
            )
        ).wasAcknowledged()

    override suspend fun getLastReading(networkId: String, nodeId: Int): Reading? =
        readingCollection.find(and(
            ReadingDao::networkId eq networkId,
            ReadingDao::nodeId eq nodeId
        )).descendingSort(ReadingDao::timestamp)
        .limit(1).first()
        ?.getReading()

    override fun getReadings(networkId: String, nodeId: Int): Flow<Reading> =
        readingCollection.find(and(
            ReadingDao::networkId eq networkId,
            ReadingDao::nodeId eq nodeId
        )).descendingSort(ReadingDao::timestamp)
        .toFlow().map(ReadingDao::getReading)

    override suspend fun saveReading(reading: Reading): Boolean =
        readingCollection.insertOne(ReadingDao.of(reading)).wasAcknowledged()

    override fun getMessagesAndErrors(networkId: String, nodeId: Int): Flow<Message> =
        messageCollection.find(and(
            MessageDao::networkId eq networkId,
            MessageDao::nodeId eq nodeId
        )).descendingSort(MessageDao::timestamp)
        .toFlow().map(MessageDao::getMessage)

    override fun getMessages(networkId: String, nodeId: Int): Flow<Message> =
        messageCollection.find(and(
            MessageDao::msgType eq Message.MessageType.Info,
            MessageDao::networkId eq networkId,
            MessageDao::nodeId eq nodeId
        )).descendingSort(MessageDao::timestamp)
        .toFlow().map(MessageDao::getMessage)

    override suspend fun getLastMessage(networkId: String, nodeId: Int): Message? =
        messageCollection.find(and(
            MessageDao::msgType eq Message.MessageType.Info,
            MessageDao::networkId eq networkId,
            MessageDao::nodeId eq nodeId
        )).descendingSort(MessageDao::timestamp)
            .limit(1).first()
            ?.getMessage()

    override fun getErrors(networkId: String, nodeId: Int): Flow<Message> =
        messageCollection.find(and(
            MessageDao::msgType eq Message.MessageType.Error,
            MessageDao::networkId eq networkId,
            MessageDao::nodeId eq nodeId
        )).descendingSort(MessageDao::timestamp)
            .toFlow().map(MessageDao::getMessage)

    override suspend fun saveMessage(msg: Message): Boolean =
        messageCollection.insertOne(MessageDao.of(msg)).wasAcknowledged()


    override suspend fun getLastError(networkId: String, nodeId: Int): Message? =
        messageCollection.find(and(
            MessageDao::msgType eq Message.MessageType.Info,
            MessageDao::networkId eq networkId,
            MessageDao::nodeId eq nodeId
        )).descendingSort(MessageDao::timestamp)
            .limit(1).first()
            ?.getMessage()


    override fun stop() {
        client.close()
    }

    override suspend fun saveDeviceSpecifications(specs: List<DeviceSpecs>): Boolean =
        specs.mapNotNull{
            deviceSpecCollection.save(DeviceSpecsDao.of(it))
        }.isNotEmpty()

    private suspend fun insertDeviceSpecifications(specs: List<DeviceSpecsDao>) {
        deviceSpecCollection.drop()
        deviceSpecCollection.insertMany(specs)
    }

    override suspend fun getDeviceSpecification(
        networkProtocol: NetworkProtocol,
        manufacturerId: Int,
        productTypeId: Int,
        productId: Int
    ): DeviceSpecs? =
        deviceSpecCollection.findOne(and(
            DeviceSpecsDao::networkProtocol eq networkProtocol,
            DeviceSpecsDao::manufacturerId eq manufacturerId,
            DeviceSpecsDao::productTypeId eq productTypeId,
            DeviceSpecsDao::productId eq productId
        ))?.getDeviceSpecs()

    override suspend fun saveManufacturers(ents: List<Manufacturer>): Boolean =
        ents.mapNotNull{
            manufacturerCollection.save(ManufacturerDao.of(it))
        }.isNotEmpty()

    private suspend fun insertManufacturers(daos: List<ManufacturerDao>) {
        manufacturerCollection.drop()
        manufacturerCollection.insertMany(daos)
    }

    override suspend fun getManufacturer(networkProtocol: NetworkProtocol, code: Int): Manufacturer? =
        manufacturerCollection.findOne(and(
            ManufacturerDao::networkProtocol eq networkProtocol,
            ManufacturerDao::code eq code
        ))?.getManufacturer()

    override suspend fun saveDeviceClasses(classes: List<DeviceClass>): Boolean =
        classes.mapNotNull{
            deviceClassCollection.save(DeviceClassDao.of(it))
        }.isNotEmpty()

    private suspend fun insertDeviceClasses(daos: List<DeviceClassDao>) {
        deviceClassCollection.drop()
        deviceClassCollection.insertMany(daos)
    }

    override suspend fun getDeviceClass(networkProtocol: NetworkProtocol, label: String): DeviceClass? =
        deviceClassCollection.findOne(and(
            DeviceSpecsDao::networkProtocol eq networkProtocol,
            DeviceSpecsDao::label eq label
        ))?.getDeviceClass()

    override fun getLogs(from: Date, to: Date): Flow<LogRecord> =
        recentLogsCollection.find(and(
            LogRecordDao::timestamp gt from,
            LogRecordDao::timestamp lte to,
        )).toFlow().map(LogRecordDao::getRecord)

    override suspend fun isCollectionEmpty(collection: String): Boolean = when (collection) {
        networkCollectionName -> networkCollection.countDocuments() < 1
        nodeCollectionName -> nodeCollection.countDocuments() < 1
        readingCollectionName -> readingCollection.countDocuments() < 1
        messageCollectionName -> messageCollection.countDocuments() < 1
        deviceSpecCollectionName -> deviceSpecCollection.countDocuments() < 1
        deviceClassCollectionName -> deviceClassCollection.countDocuments() < 1
        manufacturerCollectionName -> manufacturerCollection.countDocuments() < 1
        recentLogsCollectionName -> recentLogsCollection.countDocuments() < 1
        else -> true
    }
}

fun Application.initMongo() = pluginOrNull(Mongo) ?: install(Mongo)
