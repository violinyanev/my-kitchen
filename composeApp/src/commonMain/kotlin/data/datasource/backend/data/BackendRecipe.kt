package data.datasource.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class BackendRecipe(val id: Long, val title: String, val body: String, val timestamp: Long)
