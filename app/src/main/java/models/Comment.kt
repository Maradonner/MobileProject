package models

data class Comment(
    val id: String,
    val content: String,
    val userId: String,
    val userName: String,
    val createdAt: String,
    val replies: List<Comment>?
)