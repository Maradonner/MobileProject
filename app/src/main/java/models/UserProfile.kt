package models

data class Profile(
    val discounts: List<Discount>? = null,
    val totalDiscounts: Int,
    val ranking: Int,
    val highestRating: Double?,
    val lowestRating: Double?,
    val averageRating: Double?,
    val totalReactions: Int
)
