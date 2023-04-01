package io.sh.abodeiq.plugins.networkManual

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.sh.abodeiq.plugins.dao
import io.sh.abodeiq.plugins.networkManual.model.NetworkResp
import io.sh.abodeiq.plugins.networkManual.model.NodeResp
import io.sh.abodeiq.plugins.networkManual.model.ReadingReq
import io.sh.abodeiq.plugins.networkManual.model.ReadingResp
import kotlinx.coroutines.flow.map

val notFoundResp: (suspend (ApplicationCall) -> Unit) = {call -> call.respond(HttpStatusCode.NotFound) }
val badRequestResp: (suspend (ApplicationCall, String) -> Unit) = {call, msg -> call.respond(HttpStatusCode.BadRequest, msg) }

suspend fun handleGetNetwork(networkId: String, call: ApplicationCall) = call.application.dao
    .getNetwork(networkId)
    ?.let{call.respond(HttpStatusCode.OK, NetworkResp.fromDto(it))}
    ?: notFoundResp(call)

suspend fun handleGetNodeInfo(networkId: String, call: ApplicationCall) = call
    .parameters["nodeId"]
    ?.toIntOrNull()
    ?.let {call.application.dao.getNode(networkId, it)}
    ?.let{call.respond(HttpStatusCode.OK, NodeResp.fromDto(it))}
    ?: notFoundResp(call)

suspend fun handleGetNodeReadings(networkId: String, call: ApplicationCall) = call
    .parameters["nodeId"]
    ?.toIntOrNull()
    ?.let {call.application.dao.getReadings(networkId, it)}
    ?.map { ReadingResp.fromDto(it)}
    ?.let{call.respond(HttpStatusCode.OK, it)}
    ?: notFoundResp(call)

suspend fun handleGetLastNodeReading(networkId: String, call: ApplicationCall) = call
    .parameters["nodeId"]
    ?.toIntOrNull()
    ?.let {call.application.dao.getLastReading(networkId, it)}
    ?.let{call.respond(HttpStatusCode.OK, ReadingResp.fromDto(it))}
    ?: notFoundResp(call)

suspend fun handlePostNodeReadings(networkId: String, call: ApplicationCall) = call
    .receive(ReadingReq::class)
    .let{
        it.networkId = networkId
        call.application.dao.saveReading(it.toDto())
    }.let{call.respond(if(it) HttpStatusCode.Created else HttpStatusCode.UnprocessableEntity)}
