package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.Reading
import io.sh.abodeiq.serialisation.BsonJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class ReadingDao(
    @BsonId @Transient
    val _id: Id<ReadingDao>? = null,
    val networkId: String,
    val nodeId: Int,
    val timestamp: Long,
    val readingType: String,
    val readingTypeKeys: Pair<Int, Int>? = null,
    val readingClass: String,
    @Serializable(BsonJsonSerializer::class)
    val reading: org.bson.json.JsonObject
) {
    companion object {
        fun of(r: Reading) = ReadingDao(
            null,
            r.networkId,
            r.nodeId,
            r.timestamp,
            r.readingType,
            r.readingTypeKeys,
            r.readingClass,
            org.bson.json.JsonObject(r.reading.toString())
        )
    }

    fun getReading() = Reading(
        networkId,
        nodeId,
        timestamp,
        readingType,
        readingTypeKeys,
        readingClass,
        Json.parseToJsonElement(reading.json)
    )
}
