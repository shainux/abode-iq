@file:Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.

package io.sh.abodeiq

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.sh.abodeiq.model.DaoFacade
import io.sh.abodeiq.plugins.*
import io.sh.abodeiq.plugins.mongo.Mongo
import io.sh.abodeiq.plugins.mongo.mongoConfigName
import java.time.Clock
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

val startupTimeMs: Long = Clock.systemUTC().millis()

fun Application.module() {
    val appConfig = configureApp()

    configureSecurity()
    configureMonitoring()
    configureSerialization()
    install(Mongo){ cfg = appConfig.config(mongoConfigName) }
    configureRouting()
    configureIotNetworks(appConfig.configList("networks"))
}
val Application.dao: DaoFacade
    get() = Mongo.instance ?: throw java.lang.IllegalStateException("Mongo Plugin is not initialised")

val Application.startupTime: Date
    get() = Date(startupTimeMs)
