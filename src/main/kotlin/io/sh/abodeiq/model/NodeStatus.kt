package io.sh.abodeiq.model

/**
 * Specifies status of node:
 *   Online  - node reported itself connected within timeout interval,
 *   Offline - node is an offline device that is not expected to deliver messages automatically,
 *   Failed  - network marked node as failed,
 *   Disconnected - node failed to report itself connected within timeout interval,
 *   Unknown - status of node was never specified.
 */
@Suppress("unused")
enum class NodeStatus {
    Online, Offline, Failed, Disconnected, Unknown;
}
