package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Reading(
    val networkId: String,
    val nodeId: Int,
    val timestamp: Long,
    val readingType: String,
    val readingTypeKeys: Pair<Int, Int>? = null,
    val readingClass: String,
    val reading: JsonElement
)
