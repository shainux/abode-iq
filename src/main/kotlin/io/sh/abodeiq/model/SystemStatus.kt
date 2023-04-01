package io.sh.abodeiq.model

import io.sh.abodeiq.serialisation.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class SystemStatus(
    val systemStatus: String = "OK",
    @Serializable(DateSerializer::class)
    val upSince: Date,
    val activeNetworks: List<Network>
)
