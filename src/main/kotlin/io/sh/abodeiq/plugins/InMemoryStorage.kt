package io.sh.abodeiq.plugins

import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.util.*
import io.sh.abodeiq.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.util.*

class InMemoryStorage : DaoFacade {
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, InMemoryStorage> {
        var instance: InMemoryStorage? = null

        override val key = AttributeKey<InMemoryStorage>("InMemoryStoragePlugin")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): InMemoryStorage {
            instance = instance ?: InMemoryStorage()
            return instance!!
        }
    }

    private var networkCollection = mutableListOf<Network>()
    private val nodeCollection = mutableListOf<Node>()
    private val readingCollection = mutableListOf<Reading>()
    private val messageCollection = mutableListOf<Message>()
    private var deviceSpecCollection = mutableListOf<DeviceSpecs>()
    private var manufacturerCollection = mutableListOf<Manufacturer>()
    private var deviceClassCollection = mutableListOf<DeviceClass>()
    private var recentLogsCollection = mutableListOf<LogRecord>()

    override suspend fun getNetwork(networkId: String): Network? =
        networkCollection.firstOrNull { it.id == networkId }

    override suspend fun getNetworks(): Flow<Network> {
        val latestActivities = nodeCollection
            .map { it.networkId to it.lastActivityTs }
            .groupBy { it.first }
            .entries.associate { it.key to it.value.maxOf { it1 -> it1.second } }


        val networks = flow {
            for (nw in networkCollection) {
                latestActivities[nw.id]?.let { nw.lastActivity = Date(it) }
                emit(nw)
            }
        }
        return networks
    }

    override fun cleanNetworks(): Unit = runBlocking {
        networkCollection = mutableListOf()
    }

    override fun saveNetwork(nw: Network): Boolean = runBlocking {
        networkCollection.add(nw)
    }

    override fun deleteNetwork(networkId: String): Boolean = runBlocking {
        networkCollection.removeIf { it.id == networkId }
    }

    override suspend fun getNode(networkId: String, nodeId: Int): Node? =
        nodeCollection.firstOrNull { it.nodeId == nodeId && it.networkId == networkId }

    override suspend fun saveNode(node: Node): Boolean {
        if (nodeCollection.count { it.nodeId == node.nodeId } > 0) {
            nodeCollection.removeIf { it.nodeId == node.nodeId }
        }
        return nodeCollection.add(node)
    }

    override suspend fun getLastReading(networkId: String, nodeId: Int): Reading? = readingCollection
        .filter { it.nodeId == nodeId && it.networkId == networkId }
        .sortedByDescending(Reading::timestamp)
        .firstOrNull()

    override fun getReadings(networkId: String, nodeId: Int): Flow<Reading> {
        val readings = readingCollection
            .filter { it.nodeId == nodeId && it.networkId == networkId }
            .sortedByDescending(Reading::timestamp)
        return flow {
            for (r in readings) {
                emit(r)
            }
        }
    }

    override suspend fun saveReading(reading: Reading): Boolean = readingCollection.add(reading)

    override suspend fun getMessage(networkId: String, nodeId: Int): Message? =
        messageCollection.firstOrNull { it.nodeId == nodeId && it.networkId == networkId }

    override suspend fun saveMessage(msg: Message): Boolean = messageCollection.add(msg)

    override fun getMessagesAndErrors(networkId: String, nodeId: Int): Flow<Message> {
        val msgs = messageCollection
            .filter { it.networkId == networkId && it.nodeId == nodeId }
            .sortedByDescending(Message::timestamp)

        return flow {
            for (ms in msgs) {
                emit(ms)
            }
        }
    }

    override fun getMessages(networkId: String, nodeId: Int): Flow<Message> {
        val msgs = messageCollection
            .filter { it.networkId == networkId && it.nodeId == nodeId && it.msgType == Message.MessageType.Info }
            .sortedByDescending(Message::timestamp)
        return flow {
            for (ms in msgs) {
                emit(ms)
            }
        }
    }

    override suspend fun getLastMessage(networkId: String, nodeId: Int): Message? = messageCollection
        .filter { it.networkId == networkId && it.nodeId == nodeId && it.msgType == Message.MessageType.Info }
        .sortedByDescending(Message::timestamp)
        .firstOrNull()

    override fun getErrors(networkId: String, nodeId: Int): Flow<Message> {
        val msgs = messageCollection
            .filter { it.networkId == networkId && it.nodeId == nodeId && it.msgType == Message.MessageType.Error }
            .sortedByDescending(Message::timestamp)
        return flow {
            for (ms in msgs) {
                emit(ms)
            }
        }
    }

    override suspend fun getLastError(networkId: String, nodeId: Int): Message? = messageCollection
        .filter { it.networkId == networkId && it.nodeId == nodeId && it.msgType == Message.MessageType.Error }
        .sortedByDescending(Message::timestamp)
        .firstOrNull()

    override fun stop() {}

    override suspend fun saveDeviceSpecifications(specs: List<DeviceSpecs>): Boolean {
        deviceSpecCollection = mutableListOf()
        return deviceSpecCollection.addAll(specs)
    }

    override suspend fun getDeviceSpecification(networkProtocol: NetworkProtocol, manufacturerId: Int, productTypeId: Int, productId: Int): DeviceSpecs? =
        deviceSpecCollection.firstOrNull {
            it.networkProtocol == networkProtocol
                    && it.manufacturerId == manufacturerId
                    && it.productTypeId == productTypeId
                    && it.productId == productId
        }

    override suspend fun saveManufacturers(ents: List<Manufacturer>): Boolean {
        manufacturerCollection = mutableListOf()
        return manufacturerCollection.addAll(ents)
    }

    override suspend fun getManufacturer(networkProtocol: NetworkProtocol, code: Int): Manufacturer? =
        manufacturerCollection
            .firstOrNull { it.networkProtocol == networkProtocol && it.code == code }

    override suspend fun saveDeviceClasses(classes: List<DeviceClass>): Boolean {
        deviceClassCollection = mutableListOf()
        return deviceClassCollection.addAll(classes)
    }

    override suspend fun getDeviceClass(networkProtocol: NetworkProtocol, label: String): DeviceClass? =
        deviceClassCollection
            .firstOrNull { it.networkProtocol == networkProtocol && it.label == label }

    override fun getLogs(from: Date, to: Date): Flow<LogRecord> {
        val logs = recentLogsCollection.filter { it.timestamp > from && it.timestamp <= to }
        return flow {
            for (l in logs) {
                emit(l)
            }
        }
    }

    override suspend fun isCollectionEmpty(collection: String): Boolean = when (collection) {
        networkCollectionName -> networkCollection.size == 0
        nodeCollectionName -> nodeCollection.size == 0
        readingCollectionName -> readingCollection.size == 0
        messageCollectionName -> messageCollection.size == 0
        deviceSpecCollectionName -> deviceSpecCollection.size == 0
        deviceClassCollectionName -> deviceClassCollection.size == 0
        manufacturerCollectionName -> manufacturerCollection.size == 0
        recentLogsCollectionName -> recentLogsCollection.size == 0
        else -> true
    }
}

fun Application.initInMemoryStorage() = pluginOrNull(InMemoryStorage) ?: install(InMemoryStorage)


@Suppress("unused")
val Application.dao: DaoFacade
    get() = InMemoryStorage.instance ?: throw IllegalStateException("DAO is not initialised")
