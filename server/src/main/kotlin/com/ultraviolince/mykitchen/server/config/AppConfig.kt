package com.ultraviolince.mykitchen.server.config

data class AppConfig(
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val databaseDriver: String,
    /** Null means allow any origin (development only). In production set CORS_ALLOWED_ORIGINS. */
    val corsAllowedOrigins: List<String>?,
    /** Base URL of the OpenAI-compatible LLM server used for recipe enrichment (e.g. http://localhost:8080 for llama.cpp). */
    val ollamaBaseUrl: String,
    /** Model name passed to the LLM server for enrichment (e.g. gemma4:26b). */
    val ollamaModel: String,
    /** Null disables Unsplash image fetching (enrichments still work without images). */
    val unsplashAccessKey: String?,
    /** True only in local development; allows omitting CORS_ALLOWED_ORIGINS. Set DEV_MODE=true to enable. */
    val devMode: Boolean = false,
) {
    companion object {
        fun fromEnvironment(): AppConfig = AppConfig(
            jwtSecret = System.getenv("JWT_SECRET")
                ?: error("JWT_SECRET environment variable is required. Set it to a strong random secret."),
            jwtIssuer = System.getenv("JWT_ISSUER") ?: "mykitchen",
            jwtAudience = System.getenv("JWT_AUDIENCE") ?: "mykitchen-users",
            databaseUrl = System.getenv("DATABASE_URL")
                ?: "jdbc:postgresql://localhost:5432/mykitchen",
            databaseUser = System.getenv("DATABASE_USER") ?: "mykitchen",
            databasePassword = System.getenv("DATABASE_PASSWORD") ?: "mykitchen",
            databaseDriver = System.getenv("DATABASE_DRIVER")
                ?: "org.postgresql.Driver",
            corsAllowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS")
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.takeIf { it.isNotEmpty() },
            ollamaBaseUrl = System.getenv("OLLAMA_BASE_URL") ?: "http://ollama:11434",
            ollamaModel = System.getenv("OLLAMA_MODEL") ?: "gemma4:26b",
            unsplashAccessKey = System.getenv("UNSPLASH_ACCESS_KEY"),
            devMode = System.getenv("DEV_MODE")?.lowercase() == "true",
        )
    }
}
