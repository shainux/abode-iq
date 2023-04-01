package io.sh.abodeiq.plugins.networkManual.model

import io.ktor.server.plugins.*
import io.sh.abodeiq.model.Reading
import io.sh.abodeiq.plugins.networkManual.reading.HcaReport
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.Clock

@Serializable
data class ReadingReq(
    var networkId: String? = null,
    val nodeId: Int,
    val readingType: String,
    val reading: JsonElement
) {

    fun toDto(): Reading {
        val clazz = when (readingType){
            "HCA.Report" -> HcaReport::class.java.name
            else -> throw BadRequestException("Unexpected reading type: $readingType")
        }
        return Reading(
            null,
            networkId ?: throw IllegalArgumentException("Network ID was not specified"),
            nodeId,
            Clock.systemUTC().millis(),
            readingType,
            null,
            clazz,
            reading.toString()
        )
    }
}
