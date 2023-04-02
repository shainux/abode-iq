package io.sh.abodeiq.model

import io.ktor.http.*
import io.sh.abodeiq.serialisation.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*

@Serializable
data class Network(
    val id: String,
    val networkProtocol: NetworkProtocol,
    val name: String,
    val description: String? = null,
    var uri: String,
    val hasController: Boolean,
    val primaryControllerNodeId: Int? = null,
    val details: JsonElement? = null,
    @Serializable(DateSerializer::class)
    var lastActivity: Date? = null
)
