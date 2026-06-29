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
    val anthropicApiKey: String,
    /** Null disables Unsplash image fetching (enrichments still work without images). */
    val unsplashAccessKey: String?,
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
            anthropicApiKey = System.getenv("ANTHROPIC_API_KEY")
                ?: error("ANTHROPIC_API_KEY environment variable is required."),
            unsplashAccessKey = System.getenv("UNSPLASH_ACCESS_KEY"),
        )
    }
}
