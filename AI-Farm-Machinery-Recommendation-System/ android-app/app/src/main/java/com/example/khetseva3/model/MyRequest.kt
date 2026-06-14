package com.example.khetseva3.model

import com.google.gson.annotations.SerializedName

data class MyRequest(

    val id: Int,

    @SerializedName("machine_id")
    val machineId: Int,

    val status: String,

    @SerializedName("machine_name")
    val machineName: String,

    @SerializedName("owner_name")
    val ownerName: String,

    @SerializedName("owner_phone")
    val ownerPhone: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("hp_range")
    val hpRange: Int?,

    @SerializedName("cutting_width")
    val cuttingWidth: Double?,

    @SerializedName("working_width")
    val workingWidth: Double?,

    @SerializedName("row_count")
    val rowCount: Int?,

    @SerializedName("owner_email")
    val ownerEmail: String,

    val location: String,

    @SerializedName("price_per_hour")
    val pricePerHour: Int,

    @SerializedName("price_per_day")
    val pricePerDay: Int,

    @SerializedName("price_per_week")
    val pricePerWeek: Int,

    @SerializedName("price_per_month")
    val pricePerMonth: Int
)