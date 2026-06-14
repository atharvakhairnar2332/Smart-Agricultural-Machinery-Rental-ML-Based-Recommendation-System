package com.example.khetseva3.model

import com.google.gson.annotations.SerializedName

data class Machine(
    @SerializedName("recommended")
    val recommended: Boolean? = null,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("model_name")
    val model_name: String? = null,

    @SerializedName("hp_range")
    val hp_range: Int? = null,

    @SerializedName("cutting_width")
    val cutting_width: Double? = null,

    @SerializedName("working_width")
    val working_width: Double? = null,

    @SerializedName("row_count")
    val row_count: Int? = null,

    @SerializedName("price_per_hour")
    val price_per_hour: Int? = null,

    @SerializedName("price_per_day")
    val price_per_day: Int? = null,

    @SerializedName("price_per_week")
    val price_per_week: Int? = null,

    @SerializedName("price_per_month")
    val price_per_month: Int? = null,

    @SerializedName("owner_name")
    val owner_name: String? = null,

    @SerializedName("owner_phone")
    val owner_phone: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("owner_email")
    val owner_email: String? = null,

    @SerializedName("image_url")
    val image_url: String? = null


)