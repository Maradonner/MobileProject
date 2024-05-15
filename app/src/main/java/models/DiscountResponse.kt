package models

import com.google.gson.annotations.SerializedName

class DiscountResponse {
    @SerializedName("discounts")
    var discounts: List<Discount>? = null
}
