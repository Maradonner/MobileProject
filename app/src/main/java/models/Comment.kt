package models

data class Comment(
    val id: String? = null,
    var content: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val createdAt: String? = null,
    val replies: List<Comment>? = null,
)