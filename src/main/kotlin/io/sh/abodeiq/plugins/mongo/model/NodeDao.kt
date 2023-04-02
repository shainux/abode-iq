package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.Node
import io.sh.abodeiq.model.NodeStatus
import io.sh.abodeiq.serialisation.BsonJsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class NodeDao(
    @BsonId @Transient
    val _id: Id<NodeDao>? = null,
    val networkId: String,
    val nodeId: Int,
    val nodeStatus: NodeStatus,
    val lastActivityTs: Long,
    val lastWakeUpReason: String?,
    val nodeWakeUpIntervalMs: Int,
    val nodeStayAwakeIntervalMs: Int,
    val isPrimaryController: Boolean,
    @Serializable(BsonJsonSerializer::class)
    val details: org.bson.json.JsonObject? = null
){
    companion object {
        fun of(n: Node) = NodeDao(
            null,
            n.networkId,
            n.nodeId,
            n.nodeStatus,
            n.lastActivityTs,
            n.lastWakeUpReason,
            n.nodeWakeUpIntervalMs,
            n.nodeStayAwakeIntervalMs,
            n.isPrimaryController,
            n.details?.let{org.bson.json.JsonObject(it.toString())}
        )
    }

    fun getNode() = Node(
        networkId,
        nodeId,
        nodeStatus,
        lastActivityTs,
        lastWakeUpReason,
        nodeWakeUpIntervalMs,
        nodeStayAwakeIntervalMs,
        isPrimaryController,
        details?.let { Json.parseToJsonElement(it.json) }
    )
}
