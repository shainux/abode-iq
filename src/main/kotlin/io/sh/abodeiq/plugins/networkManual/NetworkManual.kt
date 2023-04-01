package io.sh.abodeiq.plugins.networkManual

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.sh.abodeiq.model.Network
import io.sh.abodeiq.model.NetworkProtocol
import java.time.Clock

class NetworkManual(private val cfg: Configuration) {

    data class Configuration(
        var id: String? = null,
        var name: String? = null,
        var description: String? = null,
        var app: Application? = null
    )

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, NetworkManual> {
        override val key = AttributeKey<NetworkManual>("NetworkManualPlugin")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): NetworkManual {
            val cfg = Configuration().apply(configure)
            if(cfg.id.isNullOrBlank()) {
                throw ApplicationConfigurationException("Invalid Manual Network Plugin config: invalid network ID")
            }
            return NetworkManual(cfg)
        }
    }

    init {
        val nwId = cfg.id?: throw IllegalStateException("Missing network ID")
        cfg.app?.routing {
            route("/networks/${nwId.encodeURLPathPart()}") {
                get { handleGetNetwork(nwId, call) }
                route("/{nodeId}") {
                    get("/info") { handleGetNodeInfo(nwId, call) }
                    get("/readings") { handleGetNodeReadings(nwId, call) }
                    get("/readings/last") { handleGetLastNodeReading(nwId, call) }
                    post("/readings") { handlePostNodeReadings(nwId, call) }
                }
            }
        }
    }

    fun toNetworkDto() = Network(
        id = cfg.id!!,
        networkProtocol = NetworkProtocol.Manual,
        name = cfg.name?:"ManualNetwork${Clock.systemUTC().millis()}",
        description = cfg.description,
        hasController = false
    )
}
