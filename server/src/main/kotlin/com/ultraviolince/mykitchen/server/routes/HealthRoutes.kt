package com.ultraviolince.mykitchen.server.routes

import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class VersionResponse(val version: String)

fun Route.healthRoutes() {
    get("/health") {
        call.respondText("OK")
    }
    get("/version") {
        call.respond(VersionResponse("2.0.0"))
    }
}
