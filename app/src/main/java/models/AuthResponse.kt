package models

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val refreshToken: String?,
    val tokenExpires: String?
)