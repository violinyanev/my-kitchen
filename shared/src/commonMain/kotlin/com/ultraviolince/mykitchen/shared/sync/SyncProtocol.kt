package com.ultraviolince.mykitchen.shared.sync

import kotlinx.serialization.Serializable

@Serializable
sealed interface SyncEvent {
    @Serializable
    data class Upsert(val id: Long, val version: Long) : SyncEvent

    @Serializable
    data class Delete(val id: Long, val version: Long) : SyncEvent
}

