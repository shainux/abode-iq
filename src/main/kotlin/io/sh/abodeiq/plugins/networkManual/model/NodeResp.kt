package io.sh.abodeiq.plugins.networkManual.model

import io.sh.abodeiq.model.Node
import io.sh.abodeiq.model.NodeStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class NodeResp(
    val id: Int,
    val networkId: String,
    val nodeStatus: NodeStatus,
    val lastActivityTs: Long,
    val details: Details
) {
    companion object{
        fun fromDto(e: Node) = NodeResp(
            e.nodeId, e.networkId, e.nodeStatus, e.lastActivityTs, Details.fromJson(e.details)
        )
    }

    @Serializable
    data class Details(
        val manufacturerId: Int,
        val manufacturerName: String,
        val deviceModel: String
    ){
        companion object{
            fun fromJson(json: JsonElement): Details = Json.decodeFromJsonElement<Details>(json)
        }
    }
}
