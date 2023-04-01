package io.sh.abodeiq.plugins

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.sh.abodeiq.handler.handleGetNetworks
import io.sh.abodeiq.handler.handleGetSerialPorts
import io.sh.abodeiq.handler.handleSystemShutdown
import io.sh.abodeiq.handler.handleSystemStatus

fun Application.configureRouting() {
    routing {
        route("/system"){
            get("/status") { handleSystemStatus(call) }
            get("/serial-ports") { handleGetSerialPorts(call) }
            get("/shutdown") { handleSystemShutdown(call) }
        }
        route("/networks") {
            get{ handleGetNetworks(call) }
        }
        get("/") { handleSystemStatus(call) }
        get("/health") { call.respondText("OK") }
    }
}
