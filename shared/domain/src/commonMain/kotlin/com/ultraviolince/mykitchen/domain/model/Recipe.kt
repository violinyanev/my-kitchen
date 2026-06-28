package com.ultraviolince.mykitchen.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val synced: Boolean = false,
    val deleted: Boolean = false,
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
        fun create(title: String, content: String): Recipe = Recipe(
            id = Uuid.random().toString(),
            title = title,
            content = content,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            synced = false,
            deleted = false,
        )
    }
}

