package io.sh.abodeiq.plugins.networkManual.model

import io.sh.abodeiq.model.Network
import io.sh.abodeiq.model.NetworkProtocol
import kotlinx.serialization.Serializable

@Serializable
data class NetworkResp(
    val id: String,
    val protocol: NetworkProtocol,
    val name: String,
    val description: String?,
    val hasController: Boolean
) {
    companion object{
        fun fromDto(e: Network) = NetworkResp(e.id, e.networkProtocol, e.name, e.description, e.hasController)
    }
}
