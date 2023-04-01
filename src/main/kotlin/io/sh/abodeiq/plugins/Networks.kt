package io.sh.abodeiq.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.sh.abodeiq.model.NetworkProtocol
import io.sh.abodeiq.plugins.mongo.dao
import io.sh.abodeiq.plugins.networkManual.NetworkManual

fun Application.configureIotNetworks(){
    val self = this
    self.dao.cleanNetworks()
    try {
        val networks = this@configureIotNetworks.environment.config.configList("networks")
        if(networks.isEmpty()){
            this@configureIotNetworks.log.info("Empty IoT network configuration block: nothing to plug in.")
            return
        }
        for (nw in networks){
            nw.tryGetString("type")?.let{
                val activeNw = when (NetworkProtocol.findForName(it)){
                    NetworkProtocol.Manual -> install(NetworkManual){
                        id = nw.tryGetString("id")
                        name = nw.tryGetString("name")
                        app = self
                    }
                    NetworkProtocol.ZWave -> install(NetworkManual)
                    NetworkProtocol.Ble -> install(NetworkManual)
                }.toNetworkDto()
                self.dao.saveNetwork(activeNw)
            } ?: run{
                this@configureIotNetworks.log.error("Corrupted network config: ${nw.toMap().entries.joinToString(";")}")
            }

        }
    } catch (acex:ApplicationConfigurationException){
        this@configureIotNetworks.log.info("No IoT network configuration found: nothing to plug in.")
    }
}
