package com.ultraviolince.mykitchen.recipes.domain.model

/**
 * Represents the synchronization status of a recipe with the backend server
 */
enum class SyncStatus {
    /**
     * Recipe has not been synced to the backend yet
     */
    NOT_SYNCED,

    /**
     * Recipe is currently being synced to the backend
     */
    SYNCING,

    /**
     * Recipe has been successfully synced to the backend
     */
    SYNCED,

    /**
     * Recipe sync failed due to an error
     */
    SYNC_ERROR
}
