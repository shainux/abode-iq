package io.sh.abodeiq.plugins

import IntegrationTest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.sh.abodeiq.dao
import io.sh.abodeiq.model.Node
import io.sh.abodeiq.model.NodeStatus
import io.sh.abodeiq.plugins.networkManual.model.ReadingReq
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.sh.abodeiq.model.Reading

class NetworkManualTest : IntegrationTest() {

    @Test
    fun testNetworkInfo() {
        testApplication {
            application {
                runBlocking {
                    dao.saveNode(
                        Node(
                            "network-manual", 5,
                            lastActivityTs = Clock.systemUTC().millis(),
                            isPrimaryController = false,
                            nodeStatus = NodeStatus.Online,
                            nodeStayAwakeIntervalMs = 100,
                            nodeWakeUpIntervalMs = 200
                        )
                    )
                }
            }
            val expected = """{
            |    "id": "network-manual",
            |    "protocol": "Manual",
            |    "name": "Test network",
            |    "hasController": false
            |}""".trimMargin().trimIndent()

            client.get("/networks/network-manual").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expected, bodyAsText())
            }
        }
    }

    @Test
    fun testNodeInfo() {
        val expected = """{
        |    "id": 5,
        |    "networkId": "network-manual",
        |    "nodeStatus": "Online",
        |    "lastActivityTs": 1680345742000
        |}""".trimMargin().trimIndent()
        testApplication {
            client.get("/networks/network-manual/5/info").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expected, bodyAsText())
            }
        }
    }

    @Test
    fun testNodeReadings() {
        val expected = """[
        |    {
        |        "networkId": "network-manual",
        |        "nodeId": 7,
        |        "timestamp": 1680345742050,
        |        "readingType": "Numeric",
        |        "readingClass": "class.Report6",
        |        "reading": {
        |            "type": "Reading",
        |            "id": 2,
        |            "nodeId": "network-manual",
        |            "networkId": "7"
        |        }
        |    },
        |    {
        |        "networkId": "network-manual",
        |        "nodeId": 7,
        |        "timestamp": 1680345742040,
        |        "readingType": "Numeric",
        |        "readingClass": "class.Report5",
        |        "reading": {
        |            "type": "Reading",
        |            "id": 1,
        |            "nodeId": "network-manual",
        |            "networkId": "7"
        |        }
        |    }
        |]""".trimIndent().trimMargin()

        val sampleNwId = "network-manual"
        val nodeId = 7

        val r1 = Reading(sampleNwId, nodeId, Clock.systemUTC().millis()+40, "Numeric", null, "class.Report5", Json.parseToJsonElement("""{"type":"Reading", "id":1,  "nodeId":"$sampleNwId", "networkId":"$nodeId"}"""))
        val r2 = Reading(sampleNwId, nodeId, Clock.systemUTC().millis()+50, "Numeric", null, "class.Report6", Json.parseToJsonElement("""{"type":"Reading", "id":2,  "nodeId":"$sampleNwId", "networkId":"$nodeId"}"""))

        testApplication {
            application {
                runBlocking {
                    dao.saveReading(r1)
                    dao.saveReading(r2)
                }
            }
            client.get("/networks/$sampleNwId/$nodeId/readings").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expected, bodyAsText())
            }
        }
    }

    @Test
    fun testNodeLastReading() {
        val expected = """{
        |    "networkId": "network-manual",
        |    "nodeId": 1,
        |    "timestamp": 1680345742000,
        |    "readingType": "HCA.Report",
        |    "readingClass": "io.sh.abodeiq.plugins.networkManual.reading.HcaReport",
        |    "reading": {
        |        "value1": 12345,
        |        "value2": "2233"
        |    }
        |}""".trimMargin().trimIndent()
        testApplication {
            client.get("/networks/network-manual/1/readings/last").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expected, bodyAsText())
            }
        }
    }

    @Test
    fun testPostNodeReading() {
        val sampleReading = Json.decodeFromString<JsonElement>("""{"value1":12345, "value2":"2233"}""")
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    json()
                }
            }

            client.post("/networks/network-manual/1/readings"){
                contentType(ContentType.Application.Json)
                setBody(ReadingReq(readingType = "HCA.Report", reading = sampleReading))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
            }
        }
    }
}
