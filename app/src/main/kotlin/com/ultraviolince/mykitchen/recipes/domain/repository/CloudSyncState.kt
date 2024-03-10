package com.ultraviolince.mykitchen.recipes.domain.repository

sealed class CloudSyncState {
    data object NotLoggedIn : CloudSyncState()
    data object LoginPending : CloudSyncState()
    data object SyncInProgress : CloudSyncState()
    data object Synced : CloudSyncState()
    data class LoginFailure(val errorMessage: Int) : CloudSyncState()
    data class SyncFailure(val errorMessage: Int) : CloudSyncState()
}
