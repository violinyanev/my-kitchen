package domain.model

// TODO fix kmp @Entity
class Recipe(
    val id: Long,
    val title: String,
    val content: String,
    val timestamp: Long
    // TODO fix kmp @PrimaryKey val id: Long? = null
) {
    override fun toString() = "Recipe[$id] $title (ts $timestamp)"
}
