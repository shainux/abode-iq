package io.sh.abodeiq.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DeviceClass(
    val networkProtocol: NetworkProtocol,
    val genericClassKey: Int?,
    val specificClassKey: Int?,
    val label: String,
    val details: JsonElement? = null
)
