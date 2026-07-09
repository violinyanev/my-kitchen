package com.ultraviolince.mykitchen.server.data.services

import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.JsonPrimitive
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets

/**
 * Minimal stand-in for llama.cpp's OpenAI-compatible /v1/chat/completions
 * endpoint, so EnrichmentService can be tested without a real LLM.
 */
class FakeLlmServer private constructor(private val server: HttpServer) : AutoCloseable {

    val baseUrl: String = "http://localhost:${server.address.port}"

    override fun close() {
        server.stop(0)
    }

    companion object {
        /** Always answers with [assistantContent] as the chat completion message content. */
        fun respondingWith(assistantContent: String): FakeLlmServer {
            val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
            server.createContext("/v1/chat/completions") { exchange ->
                exchange.requestBody.readBytes() // drain request
                val escapedContent = JsonPrimitive(assistantContent).toString()
                val body = """{"choices":[{"message":{"role":"assistant","content":$escapedContent}}]}"""
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
            return FakeLlmServer(server)
        }

        /** Always answers with the given HTTP status and no useful body. */
        fun respondingWithStatus(status: Int): FakeLlmServer {
            val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
            server.createContext("/v1/chat/completions") { exchange ->
                exchange.requestBody.readBytes()
                exchange.sendResponseHeaders(status, -1)
                exchange.close()
            }
            server.start()
            return FakeLlmServer(server)
        }

        /** Returns a URL nothing is listening on, to simulate a connection failure. */
        fun unreachableUrl(): String {
            // Grab a free port and release it immediately: nothing will be
            // listening there, so a client connecting gets "connection refused".
            val port = ServerSocket(0).use { it.localPort }
            return "http://localhost:$port"
        }
    }
}
