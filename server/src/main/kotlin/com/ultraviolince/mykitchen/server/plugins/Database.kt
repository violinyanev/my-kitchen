package com.ultraviolince.mykitchen.server.plugins

import com.ultraviolince.mykitchen.server.config.AppConfig
import com.ultraviolince.mykitchen.server.data.tables.Recipes
import com.ultraviolince.mykitchen.server.data.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase(config: AppConfig) {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.databaseUrl
        username = config.databaseUser
        password = config.databasePassword
        driverClassName = config.databaseDriver
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users, Recipes)
    }
}

fun configureTestDatabase(jdbcUrl: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL") {
    Database.connect(
        url = jdbcUrl,
        driver = "org.h2.Driver",
        user = "sa",
        password = "",
    )
    transaction {
        SchemaUtils.create(Users, Recipes)
    }
}

fun dropTestDatabase() {
    transaction {
        SchemaUtils.drop(Recipes, Users)
    }
}
