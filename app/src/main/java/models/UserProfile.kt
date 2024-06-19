package models

data class Profile(
    val totalDiscounts: Int,
    val ranking: Int,
    val highestRating: Double?,
    val lowestRating: Double?,
    val averageRating: Double?,
    val totalReactions: Int
)
