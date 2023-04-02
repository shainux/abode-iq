package io.sh.abodeiq.model

import kotlinx.coroutines.flow.Flow
import java.util.*

const val networkCollectionName = "networks"
const val nodeCollectionName = "nodes"
const val readingCollectionName = "readings"
const val messageCollectionName = "messages"
const val deviceSpecCollectionName = "deviceSpecifications"
const val deviceClassCollectionName = "deviceClasses"
const val manufacturerCollectionName = "manufacturers"
const val recentLogsCollectionName = "logsRecent"
const val deviceClassDefinitions = "/definitions/deviceClasses.json"
const val deviceSpecsDefinitions = "/definitions/deviceSpecs.json"
const val manufacturersDefinitions = "/definitions/manufacturers.json"

interface DaoFacade {
    suspend fun getNetworks(): Flow<Network>
    suspend fun getNetwork(networkId: String): Network?
    fun saveNetwork(nw: Network)
    fun cleanNetworks()
    fun deleteNetwork(networkId: String): Boolean
    suspend fun getNode(networkId: String, nodeId: Int): Node?
    suspend fun saveNode(node: Node): Boolean
    suspend fun getLastReading(networkId: String, nodeId: Int): Reading?
    fun getReadings(networkId: String, nodeId: Int): Flow<Reading>
    suspend fun saveReading(reading: Reading): Boolean
    fun getMessagesAndErrors(networkId: String, nodeId: Int): Flow<Message>
    fun getMessages(networkId: String, nodeId: Int): Flow<Message>
    fun getErrors(networkId: String, nodeId: Int): Flow<Message>
    suspend fun saveMessage(msg: Message): Boolean
    suspend fun getLastMessage(networkId: String, nodeId: Int): Message?
    suspend fun getLastError(networkId: String, nodeId: Int): Message?
    suspend fun saveDeviceSpecifications(specs: List<DeviceSpecs>): Boolean
    suspend fun getDeviceSpecification(networkProtocol: NetworkProtocol,manufacturerId: Int, productTypeId: Int, productId: Int): DeviceSpecs?
    suspend fun saveManufacturers(ents: List<Manufacturer>): Boolean
    suspend fun getManufacturer(networkProtocol: NetworkProtocol, code: Int): Manufacturer?
    suspend fun saveDeviceClasses(classes: List<DeviceClass>): Boolean
    suspend fun getDeviceClass(networkProtocol: NetworkProtocol, label: String): DeviceClass?
    suspend fun isCollectionEmpty(collection: String): Boolean
    fun getLogs(from: Date, to: Date): Flow<LogRecord>
    fun stop()
}
