package models

import com.google.gson.annotations.SerializedName

class Shop {
    @SerializedName("id")
    private val id: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("description")
    private val description: String? = null

    @SerializedName("websiteUrl")
    private val websiteUrl: String? = null

    @SerializedName("logoUrl")
    private val logoUrl: String? = null // геттеры и сеттеры для всех полей
}
