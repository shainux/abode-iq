package io.sh.abodeiq.model

import kotlinx.serialization.Serializable

@Serializable
data class Manufacturer (
    val networkProtocol: NetworkProtocol,
    val code: Int,
    val label: String
)
