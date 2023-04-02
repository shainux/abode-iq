package io.sh.abodeiq.handler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.sh.abodeiq.dao
import kotlinx.coroutines.flow.toList

/**
 * @return list of io.sh.abodeiq.api.models.ActiveNetwork that provide brief
 * info on active IoT networks (Z-Wave, BLE, etc.)
 */
suspend fun handleGetNetworks(call: ApplicationCall) = call.application.dao
    .getNetworks()
    .toList()
    .let { call.respond(HttpStatusCode.OK, it) }
