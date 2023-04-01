package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import java.time.Clock

@Serializable
data class Message(
    val networkId: String,
    val nodeId: Int,
    val msgType: MessageType,
    val msg: String,
    val timestamp: Long = Clock.systemUTC().millis()
){
    enum class MessageType{ Info, Error }
}
