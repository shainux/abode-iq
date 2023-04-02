package io.sh.abodeiq.plugins

import IntegrationTest
import io.ktor.server.testing.*
import io.sh.abodeiq.model.*
import io.sh.abodeiq.plugins.mongo.Mongo
import io.sh.abodeiq.plugins.mongo.initMongo
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Clock

class MongoPluginTest : IntegrationTest() {

    private val sampleJson = Json.decodeFromString<JsonElement>("""{"key1":"val1", "key2":[1,2,3]}""")

    @Test
    fun testMongoPlugin() {
        testApplication {
            application {
                val mongo = initMongo()
                testNetworks(mongo)
                testNodes(mongo)
                testMessages(mongo)
                testReadings(mongo)
                testManufacturers(mongo)
                testDeviceClasses(mongo)
                testDeviceSpecs(mongo)
            }
        }
    }

    private fun testNetworks(mongo: Mongo) {
        val sampleNwId = "test-network"
        val sampleNetwork = Network(
            id = sampleNwId,
            networkProtocol = NetworkProtocol.ZWave,
            name = "Test Network",
            description = "Test network description",
            hasController = true,
            primaryControllerNodeId = 1,
            details = sampleJson,
            uri="/networks/$sampleNwId"
        )
        runBlocking {
            assertNull(mongo.getNetwork(sampleNwId), "Illegal state of the test dataset")
            mongo.saveNetwork(sampleNetwork)
            val actual = mongo.getNetwork(sampleNwId)
            assertEquals(sampleNetwork, actual, "Network was not retrieved correctly")
            assertTrue(mongo.deleteNetwork(sampleNwId), "Network was not deleted")
            assertNull(mongo.getNetwork(sampleNwId), "Network was not deleted")
        }
    }

    private fun testNodes(mongo: Mongo) {
        val sampleNwId = "test-network"
        val sampleNodeId = 5
        val sampleNode = Node(
            sampleNwId,
            sampleNodeId,
            NodeStatus.Online,
            Clock.systemUTC().millis(),
            "Clock",
            3000,
            1000,
            false,
            sampleJson
        )

        runBlocking {
            assertNull(mongo.getNode(sampleNwId, sampleNodeId), "Illegal state of the test dataset")
            mongo.saveNode(sampleNode)
            val actual = mongo.getNode(sampleNwId, sampleNodeId)
            assertEquals(sampleNode, actual, "Node was not retrieved correctly")
            assertTrue(mongo.deleteNode(sampleNwId, sampleNodeId), "Node was not deleted")
            assertNull(mongo.getNode(sampleNwId, sampleNodeId), "Node was not deleted")
        }
    }

    private fun testMessages(mongo: Mongo) {
        val sampleNw1Id = "test1-network"
        val sampleNw2Id = "test2-network"
        val sampleNode1Id = 5
        val sampleNode2Id = 3
        val sampleNode3Id = 6

        val sampleMessages = listOf(
            Message(sampleNw1Id, sampleNode1Id, Message.MessageType.Error, "Error #1 $sampleNw1Id, $sampleNode1Id", Clock.systemUTC().millis()),
            Message(sampleNw1Id, sampleNode2Id, Message.MessageType.Error, "Error #2 $sampleNw1Id, $sampleNode2Id", Clock.systemUTC().millis()+10),
            Message(sampleNw2Id, sampleNode1Id, Message.MessageType.Info, "Info #3 $sampleNw2Id, $sampleNode1Id", Clock.systemUTC().millis()+20),
            Message(sampleNw1Id, sampleNode1Id, Message.MessageType.Info, "Info #4 $sampleNw1Id, $sampleNode1Id", Clock.systemUTC().millis()+30),
            Message(sampleNw1Id, sampleNode1Id, Message.MessageType.Error, "Error #5 $sampleNw1Id, $sampleNode1Id", Clock.systemUTC().millis()+40),
            Message(sampleNw1Id, sampleNode2Id, Message.MessageType.Error, "Error #6 $sampleNw1Id, $sampleNode2Id", Clock.systemUTC().millis()+50),
            Message(sampleNw2Id, sampleNode1Id, Message.MessageType.Info, "Info #7 $sampleNw2Id, $sampleNode1Id", Clock.systemUTC().millis()+60),
            Message(sampleNw1Id, sampleNode1Id, Message.MessageType.Info, "Info #8 $sampleNw1Id, $sampleNode1Id", Clock.systemUTC().millis()+70),
            Message(sampleNw1Id, sampleNode1Id, Message.MessageType.Error, "Error #9 $sampleNw1Id, $sampleNode1Id", Clock.systemUTC().millis()+80),
            Message(sampleNw1Id, sampleNode2Id, Message.MessageType.Error, "Error #10 $sampleNw1Id, $sampleNode2Id", Clock.systemUTC().millis()+90),
            Message(sampleNw2Id, sampleNode1Id, Message.MessageType.Info, "Info #11 $sampleNw2Id, $sampleNode1Id", Clock.systemUTC().millis()+100),
            Message(sampleNw1Id, sampleNode3Id, Message.MessageType.Info, "Info #12 $sampleNw1Id, $sampleNode3Id", Clock.systemUTC().millis()+120),
        )

        runBlocking {
            assertTrue(mongo.getMessages(sampleNw1Id, sampleNode1Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getMessages(sampleNw1Id, sampleNode2Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getMessages(sampleNw1Id, sampleNode3Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getMessages(sampleNw2Id, sampleNode1Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getMessages(sampleNw2Id, sampleNode2Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getMessages(sampleNw2Id, sampleNode3Id).toList().isEmpty(), "Illegal state of the test dataset")

            sampleMessages.forEach{ mongo.saveMessage(it)}

            var actualList = mongo.getMessagesAndErrors(sampleNw1Id, sampleNode1Id).toList()

            assertEquals(5, actualList.size, "Wrong amount of messages was retrieved for Node")
            assertTrue(actualList.containsAll(
                listOf(sampleMessages[0], sampleMessages[3], sampleMessages[4], sampleMessages[7], sampleMessages[8])),
                "Wrong messages were retrieved for Node"
            )

            var actualItm = mongo.getLastError(sampleNw1Id, sampleNode1Id)
            assertEquals(sampleMessages[7], actualItm, "Wrong error was retrieved for Node")

            actualItm = mongo.getLastMessage(sampleNw2Id, sampleNode1Id)
            assertEquals(sampleMessages[10], actualItm, "Wrong message was retrieved for Node")

            actualList = mongo.getErrors(sampleNw1Id, sampleNode1Id).toList()

            assertEquals(3, actualList.size, "Wrong amount of errors was retrieved for Node")
            assertTrue(actualList.containsAll(
                listOf(sampleMessages[0], sampleMessages[4], sampleMessages[8])),
                "Wrong errors were retrieved for Node"
            )

            actualList = mongo.getMessages(sampleNw1Id, sampleNode1Id).toList()

            assertEquals(2, actualList.size, "Wrong amount of messages was retrieved for Node")
            assertTrue(actualList.containsAll(
                listOf(sampleMessages[3], sampleMessages[7])),
                "Wrong messages were retrieved for Node"
            )
        }
    }

    private fun testReadings(mongo: Mongo) {
        val sampleNw1Id = "test1-network"
        val sampleNw2Id = "test2-network"
        val sampleNode1Id = 5
        val sampleNode2Id = 3

        val sampleReadings = listOf(
            Reading(sampleNw1Id, sampleNode1Id, Clock.systemUTC().millis(), "Numeric", null, "class.Report1", Json.parseToJsonElement("""{"type":"Reading", "id":1,  "nodeId":"$sampleNw1Id", "networkId":"$sampleNode1Id"}""")),
            Reading(sampleNw1Id, sampleNode2Id, Clock.systemUTC().millis()+10, "Numeric", null, "class.Report2", Json.parseToJsonElement("""{"type":"Reading", "id":2,  "nodeId":"$sampleNw1Id", "networkId":"$sampleNode2Id"}""")),
            Reading(sampleNw2Id, sampleNode1Id, Clock.systemUTC().millis()+20, "Numeric", null, "class.Report3", Json.parseToJsonElement("""{"type":"Reading", "id":3,  "nodeId":"$sampleNw2Id", "networkId":"$sampleNode1Id"}""")),
            Reading(sampleNw2Id, sampleNode2Id, Clock.systemUTC().millis()+30, "Numeric", null, "class.Report4", Json.parseToJsonElement("""{"type":"Reading", "id":4,  "nodeId":"$sampleNw1Id", "networkId":"$sampleNode2Id"}""")),
            Reading(sampleNw1Id, sampleNode1Id, Clock.systemUTC().millis()+40, "Numeric", null, "class.Report5", Json.parseToJsonElement("""{"type":"Reading", "id":5,  "nodeId":"$sampleNw1Id", "networkId":"$sampleNode2Id"}""")),
            Reading(sampleNw1Id, sampleNode2Id, Clock.systemUTC().millis()+50, "Numeric", null, "class.Report6", Json.parseToJsonElement("""{"type":"Reading", "id":6,  "nodeId":"$sampleNw1Id", "networkId":"$sampleNode2Id"}"""))
        )

        runBlocking {
            assertTrue(mongo.getReadings(sampleNw1Id, sampleNode1Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getReadings(sampleNw1Id, sampleNode2Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getReadings(sampleNw2Id, sampleNode1Id).toList().isEmpty(), "Illegal state of the test dataset")
            assertTrue(mongo.getReadings(sampleNw2Id, sampleNode2Id).toList().isEmpty(), "Illegal state of the test dataset")

            sampleReadings.forEach{ mongo.saveReading(it)}

            val actualList = mongo.getReadings(sampleNw2Id, sampleNode1Id).toList()

            assertEquals(1, actualList.size, "Wrong amount of readings was retrieved for Node")
            assertTrue(actualList.containsAll(listOf(sampleReadings[2])), "Wrong readings were retrieved for Node")

            val actualItm = mongo.getLastReading(sampleNw1Id, sampleNode1Id)
            assertEquals(sampleReadings[4], actualItm, "Wrong reading was retrieved for Node")
        }
    }

    private fun testManufacturers(mongo: Mongo) {
        val expected = Manufacturer(
            NetworkProtocol.ZWave,
            123,
            "IWATSU"
        )

        runBlocking {
            val actual = mongo.getManufacturer(expected.networkProtocol, expected.code)
            assertEquals(expected, actual, "Manufacturer was not retrieved correctly")
        }
    }

    private fun testDeviceClasses(mongo: Mongo) {
        val expected = DeviceClass(
            NetworkProtocol.ZWave,
            2,
            3,
        "Static Installer Tool",
            Json.decodeFromString("{\"controlledCCs\":[\"Association\",\"Configuration\",\"Controller Replication\",\"Multi Channel\",\"Multi Channel Association\",\"Manufacturer Specific\",\"Version\",\"Wake Up\"],\"supportedCCs\":[\"Controller Replication\",\"Multi Command\",\"Manufacturer Specific\",\"Version\"],\"capabilities\":[32,133,112,33,96,142,114,134,132,33,143,114,134]}")
        )
        runBlocking {
            val actual = mongo.getDeviceClass(expected.networkProtocol, expected.label)
            assertEquals(expected, actual, "DeviceClass was not retrieved correctly")
        }
    }

    private fun testDeviceSpecs(mongo: Mongo) {
        val expected = DeviceSpecs(
            NetworkProtocol.ZWave,
            1051,
            1,
            4,
            "TUXEDOW",
            "Chandelier",
            Json.decodeFromString("{\"firmwareVersion\":{\"min\":\"0.0\",\"max\":\"255.255\"},\"manufacturer\":\"Resideo\",\"metadata\":{\"inclusion\":\"1. At the TUXEDOW Home screen:\n" +
                    " a) Press the Devices icon and then press the SETUP icon to display “Z-Wave Device Management” screen.\n" +
                    " b) Press the ADD DEVICE icon.\n" +
                    "2. At the device Module:\n" +
                    "a) Press the Function Key on the device\",\"exclusion\":\"1. At the TUXEDOW Home screen:\n" +
                    " a) Press the Devices and Setup icons to display the “ZWave Device Management” screen.\n" +
                    " b) Highlight the device to remove and press the Remove Device icon.\n" +
                    "2. At the device module:\n" +
                    " a) Press the Function Key to remove the device from the keypad\",\"reset\":\"Please use this procedure only when the network primary controller is missing or otherwise inoperable.\n" +
                    "\n" +
                    "To activate the Keypad Reset function, press the Setup, System Setup and Advanced Setup icons\n" +
                    "\n" +
                    "1. Enter your Authorized Code and press the Keypad Reset icon.\n" +
                    "\n" +
                    "2. Select OK or Cancel\",\"manual\":\"https://products.z-wavealliance.org/ProductManual/File?folder=&filename=product_documents/3624/800-25178B_QIG_TUXEDOW.pdf\"},\"zwaveAllianceIds\":[3624]}")
        )
        runBlocking {
            val actual = mongo.getDeviceSpecification(expected.networkProtocol, expected.manufacturerId, expected.productTypeId, expected.productId)
            assertEquals(expected, actual, "Device Specification was not retrieved correctly")
        }
    }
}
