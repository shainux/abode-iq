package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DeviceSpecs(
    val networkProtocol: NetworkProtocol,
    val manufacturerId: Int,
    val productTypeId: Int,
    val productId: Int,
    val label: String,
    val description: String?,
    val details: JsonElement?
)
