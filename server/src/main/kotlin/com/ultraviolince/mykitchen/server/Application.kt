package com.ultraviolince.mykitchen.server

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.plugins.configureAuthentication
import com.ultraviolince.mykitchen.server.plugins.configureCors
import com.ultraviolince.mykitchen.server.plugins.configureDatabase
import com.ultraviolince.mykitchen.server.plugins.configureSerialization
import com.ultraviolince.mykitchen.server.plugins.configureStatusPages
import com.ultraviolince.mykitchen.server.data.services.AutoBeautifyWorker
import com.ultraviolince.mykitchen.server.data.services.EnrichmentService
import com.ultraviolince.mykitchen.server.routes.authRoutes
import com.ultraviolince.mykitchen.server.routes.enrichmentRoutes
import com.ultraviolince.mykitchen.server.routes.healthRoutes
import com.ultraviolince.mykitchen.server.routes.recipeRoutes
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.math.min

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    embeddedServer(Netty, port = 5000, host = "0.0.0.0") {
        val config = AppConfig.fromEnvironment()
        configureDatabase(config)
        configureSerialization()
        configureAuthentication(config)
        configureStatusPages()
        configureCors(config)
        configureRouting(config)
        launchAutoBeautifyWorker(EnrichmentService(config))
    }.start(wait = true)
}

/**
 * Launches the [AutoBeautifyWorker] in the application scope with a supervised
 * restart loop and exponential backoff. If the worker throws an uncaught
 * exception it is logged at ERROR level and the worker is restarted after a
 * delay (starting at [BACKOFF_INITIAL_MS], doubling each crash, capped at
 * [BACKOFF_MAX_MS]).
 */
fun Application.launchAutoBeautifyWorker(enrichmentService: EnrichmentService) {
    launch {
        var backoffMs = BACKOFF_INITIAL_MS
        while (true) {
            try {
                AutoBeautifyWorker(enrichmentService).run()
                // run() exited normally (coroutine cancelled) — don't restart
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("AutoBeautifyWorker crashed, restarting in ${backoffMs / 1000}s", e)
                delay(backoffMs)
                backoffMs = min(backoffMs * 2, BACKOFF_MAX_MS)
            }
        }
    }
}

fun Application.configureRouting(config: AppConfig) {
    val enrichmentService = EnrichmentService(config)
    routing {
        healthRoutes()
        authRoutes(config)
        recipeRoutes()
        enrichmentRoutes(enrichmentService)
    }
}

private const val BACKOFF_INITIAL_MS = 5_000L
private const val BACKOFF_MAX_MS = 300_000L
