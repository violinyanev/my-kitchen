package com.ultraviolince.mykitchen.server.config

data class AppConfig(
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val databaseDriver: String,
) {
    companion object {
        fun fromEnvironment(): AppConfig = AppConfig(
            jwtSecret = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production",
            jwtIssuer = System.getenv("JWT_ISSUER") ?: "mykitchen",
            jwtAudience = System.getenv("JWT_AUDIENCE") ?: "mykitchen-users",
            databaseUrl = System.getenv("DATABASE_URL")
                ?: "jdbc:postgresql://localhost:5432/mykitchen",
            databaseUser = System.getenv("DATABASE_USER") ?: "mykitchen",
            databasePassword = System.getenv("DATABASE_PASSWORD") ?: "mykitchen",
            databaseDriver = System.getenv("DATABASE_DRIVER")
                ?: "org.postgresql.Driver",
        )
    }
}
