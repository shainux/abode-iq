package io.sh.abodeiq.model

import io.sh.abodeiq.serialisation.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class LogRecord(
    val networkId: Int,
    val nodeId: Int,
    val data: String,
    val logLevel: String,
    val logger: String,
    val msg: String,
    @Serializable(DateSerializer::class)
    val timestamp: Date
)
