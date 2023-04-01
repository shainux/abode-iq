package io.sh.abodeiq

import IntegrationTest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplicationTest : IntegrationTest() {
    private val expectedSystemStatusResponse = """{
            |    "upSince": "2023-04-01T12:42:22",
            |    "activeNetworks": [
            |        {
            |            "id": "hca-manual",
            |            "networkProtocol": "Manual",
            |            "name": "Heating Cost Allocators",
            |            "hasController": false
            |        }
            |    ]
            |}""".trimIndent().trimMargin()

    @Test
    fun testRoot() {
        testApplication {
            client.get("/health").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("OK", bodyAsText())
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expectedSystemStatusResponse, bodyAsText().trimIndent().trimMargin())
            }
        }
    }

    @Test
    fun testSystem() = testApplication {
        client.get("/system/serial-ports").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("User-Specified Port", bodyAsText())
        }
        client.get("/system/status").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(expectedSystemStatusResponse, bodyAsText().trimIndent().trimMargin())
        }
    }

    @Test
    fun testNetworks() {
        val expectedNetworksResponse = """[
            |    {
            |        "id": "hca-manual",
            |        "networkProtocol": "Manual",
            |        "name": "Heating Cost Allocators",
            |        "hasController": false
            |    }
            |]""".trimIndent().trimMargin()
        testApplication {
            client.get("/networks").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(expectedNetworksResponse, bodyAsText())
            }
        }
    }
}
