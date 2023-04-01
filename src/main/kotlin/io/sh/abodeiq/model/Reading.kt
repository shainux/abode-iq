package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class Reading(
    @BsonId @Transient
    val _id: Id<Reading>? = null,
    val networkId: String,
    val nodeId: Int,
    val timestamp: Long,
    val readingType: String,
    val readingTypeKeys: Pair<Int, Int>? = null,
    val readingClass: String,
    val reading: String
)
