package io.sh.abodeiq.plugins.mongo.model

import io.ktor.http.*
import io.sh.abodeiq.model.Network
import io.sh.abodeiq.model.NetworkProtocol
import io.sh.abodeiq.serialisation.BsonJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class NetworkDao(
    @BsonId
    @Transient
    val _id: Id<NetworkDao>? = null,
    val id: String,
    val networkProtocol: NetworkProtocol,
    val name: String,
    val description: String? = null,
    val hasController: Boolean,
    val primaryControllerNodeId: Int? = null,
    @Serializable(BsonJsonSerializer::class)
    val details: org.bson.json.JsonObject? = null
) {
    companion object {
        fun of(n: Network) = NetworkDao(
            null,
            n.id,
            n.networkProtocol,
            n.name,
            n.description,
            n.hasController,
            n.primaryControllerNodeId,
            n.details?.let{org.bson.json.JsonObject(it.toString())}
        )
    }

    fun getNetwork() = Network(
        id,
        networkProtocol,
        name,
        description,
        "/networks/${id.encodeURLPathPart()}",
        hasController,
        primaryControllerNodeId,
        details?.let { Json.parseToJsonElement(it.json) }
    )
}
