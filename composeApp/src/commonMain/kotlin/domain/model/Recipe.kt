package domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Recipe(
    val title: String,
    val content: String,
    val timestamp: Long,
    @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}
