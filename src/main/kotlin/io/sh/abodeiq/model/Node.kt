package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Node(
    val networkId: String,
    val nodeId: Int,
    val nodeStatus: NodeStatus,
    val lastActivityTs: Long,
    val lastWakeUpReason: String? = null,
    val nodeWakeUpIntervalMs: Int,
    val nodeStayAwakeIntervalMs: Int,
    val isPrimaryController: Boolean,
    val details: JsonElement? = null
)
