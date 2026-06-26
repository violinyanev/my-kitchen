package com.ultraviolince.mykitchen.server.routes

import com.ultraviolince.mykitchen.server.data.dto.VersionResponse
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes() {
    get("/health") {
        call.respondText("OK")
    }
    get("/version") {
        call.respond(VersionResponse("2.0.0"))
    }
}
