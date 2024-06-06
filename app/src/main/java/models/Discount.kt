package models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Discount : Serializable {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("rating")
    private val rating = 0.0

    @SerializedName("discountLink")
    var discountLink: String? = null

    @SerializedName("imageLink")
    var imageLink: String? = null

    @SerializedName("defaultPrice")
    var defaultPrice = 0.0

    @SerializedName("discountPrice")
    var discountPrice = 0.0

    @SerializedName("createTime")
    private val createTime: String? = null

    @SerializedName("updateTime")
    private val updateTime: String? = null

    @SerializedName("startTime")
    private val startTime: String? = null

    @SerializedName("endTime")
    private val endTime: String? = null

    @SerializedName("categories")
    private val categories: List<Category>? = null

    @SerializedName("shop")
    val shop: Shop? = null

    @SerializedName("country")
    val country: Country? = null
}
