package io.sh.abodeiq.plugins.mongo.model

@kotlinx.serialization.Serializable
data class NetworkLatestActivity (
    val _id: String,
    val _latestActivity: Long
)
