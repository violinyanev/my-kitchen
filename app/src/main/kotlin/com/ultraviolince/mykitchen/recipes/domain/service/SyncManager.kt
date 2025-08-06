package com.ultraviolince.mykitchen.recipes.domain.service

import kotlinx.coroutines.flow.Flow

interface SyncManager {
    fun isOnline(): Flow<Boolean>
    suspend fun syncWhenOnline()
    suspend fun forcSync()
}