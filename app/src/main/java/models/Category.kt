package models

import com.google.gson.annotations.SerializedName

class Category {
    @SerializedName("id")
    private val id: String? = null

    @SerializedName("name")
    private val name: String? = null

    @SerializedName("description")
    private val description: String? = null // геттеры и сеттеры для всех полей
}
