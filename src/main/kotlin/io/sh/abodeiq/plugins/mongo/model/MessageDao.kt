package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

@Serializable
data class MessageDao(
    @BsonId @Transient
    val _id: Id<MessageDao>? = null,
    val networkId: String,
    val nodeId: Int,
    val msgType: Message.MessageType,
    val msg: String,
    val timestamp: Long
) {
    companion object {
        fun of(m: Message) = MessageDao(
            null,
            m.networkId,
            m.nodeId,
            m.msgType,
            m.msg,
            m.timestamp
        )
    }

    fun getMessage() = Message(
        networkId,
        nodeId,
        msgType,
        msg,
        timestamp
    )
}
