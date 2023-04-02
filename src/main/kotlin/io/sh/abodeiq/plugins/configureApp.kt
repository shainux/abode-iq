package io.sh.abodeiq.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*

const val environmentKey = "ENV"

fun Application.configureApp(): ApplicationConfig {
    val cfgEnv = this.environment.config.tryGetString("environment")
    if(cfgEnv == "test"){
        return this.environment.config
    }
    val env = System.getenv()[environmentKey] ?: "local"
    val envConfig = ApplicationConfig("application-$env.conf")
    return this.environment.config.mergeWith(envConfig)
}
