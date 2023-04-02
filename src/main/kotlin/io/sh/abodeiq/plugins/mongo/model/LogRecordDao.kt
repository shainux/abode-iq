package io.sh.abodeiq.plugins.mongo.model

import io.sh.abodeiq.model.LogRecord
import io.sh.abodeiq.serialisation.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import java.util.*

@Serializable
data class LogRecordDao(
    @BsonId @Transient
    val _id: Id<LogRecordDao>? = null,
    val networkId: Int,
    val nodeId: Int,
    val data: String,
    val logLevel: String,
    val logger: String,
    val msg: String,
    @Serializable(DateSerializer::class)
    val timestamp: Date
) {
    companion object {
        fun of(l: LogRecord) = LogRecordDao(
            null,
            l.networkId,
            l.nodeId,
            l.data,
            l.logLevel,
            l.logger,
            l.msg,
            l.timestamp
        )
    }

    fun getRecord() = LogRecord(
        networkId,
        nodeId,
        data,
        logLevel,
        logger,
        msg,
        timestamp
    )
}
