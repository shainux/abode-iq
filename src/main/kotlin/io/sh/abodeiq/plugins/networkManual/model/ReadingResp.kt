package io.sh.abodeiq.plugins.networkManual.model

import io.sh.abodeiq.model.Reading
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
data class ReadingResp(
    val networkId: String,
    val nodeId: Int,
    val timestamp: Long,
    val readingType: String,
    val readingClass: String,
    val reading: JsonElement
) {
    companion object{
        fun fromDto(e: Reading) = ReadingResp(
            e.networkId, e.nodeId, e.timestamp, e.readingType, e.readingClass, Json.decodeFromString(e.reading)
        )
    }
}
