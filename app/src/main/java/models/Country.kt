package models

import com.google.gson.annotations.SerializedName

class Country {
    @SerializedName("id")
    private val id: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("isoCode")
    private val isoCode: String? = null

    @SerializedName("isAvailable")
    private val isAvailable = false // геттеры и сеттеры для всех полей
}
