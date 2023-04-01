@file:Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.

package io.sh.abodeiq

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.sh.abodeiq.plugins.*
import java.time.Clock
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
    configureIotNetworks()
}

val Application.startupTime: Date
    get() = Date(Clock.systemUTC().millis())
