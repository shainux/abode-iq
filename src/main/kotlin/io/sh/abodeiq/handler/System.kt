package io.sh.abodeiq.handler

import com.fazecast.jSerialComm.SerialPort.getCommPorts
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.sh.abodeiq.model.SystemStatus
import io.sh.abodeiq.plugins.mongo.dao
import io.sh.abodeiq.startupTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

const val exitCodeOnShutdown = 0

/**
 * @return io.sh.abodeiq.api.models.SystemStatus that contains brief overview of the system state
 */
suspend fun handleSystemStatus(call: ApplicationCall) = call.application.dao
    .getNetworks()
    .toList()
    .let {
        call.respond(HttpStatusCode.OK, SystemStatus("OK", call.application.startupTime, it))
    }

/**
 * Shut down the system
 * copy-paste from ShutDownUrl.ApplicationCallPlugin
 */
suspend fun handleSystemShutdown(call: ApplicationCall){
    val app = call.application
    app.log.warn("Shutdown URL was called: server is going down")

    val environment = app.environment

    val latch = CompletableDeferred<Nothing>()
    call.application.launch {
        latch.join()

        environment.monitor.raise(ApplicationStopPreparing, environment)
        if (environment is ApplicationEngineEnvironment) {
            environment.stop()
        } else {
            app.dispose()
        }

        exitProcess(exitCodeOnShutdown)
    }

    try {
        call.respond(HttpStatusCode.Gone)
    } finally {
        latch.cancel()
    }
}

/**
 * @return list of available serial ports as a string separated by comma
 */
suspend fun handleGetSerialPorts(call: ApplicationCall) = getCommPorts()
    .joinToString(", \n") { it.descriptivePortName }
    .let{ call.respondText(it) }
