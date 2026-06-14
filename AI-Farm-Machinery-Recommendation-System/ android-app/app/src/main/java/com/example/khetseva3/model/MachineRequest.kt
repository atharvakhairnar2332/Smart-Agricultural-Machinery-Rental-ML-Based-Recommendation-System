package com.example.khetseva3.model
import com.google.gson.annotations.SerializedName
data class MachineRequest(

    val id: Int,

    @SerializedName("machine_id")
    val machineId: Int,

    @SerializedName("requester_name")
    val requesterName: String,

    val type: String,

    @SerializedName("hp_range")
    val hpRange: Int?,

    @SerializedName("cutting_width")
     val cuttingWidth: Double?,

    @SerializedName("working_width")
    val workingWidth: Double?,

    @SerializedName("row_count")
    val rowCount: Int?,

    @SerializedName("requester_phone")
    val requesterPhone: String,

    @SerializedName("requester_email")
    val requesterEmail: String,

    @SerializedName("machine_name")
    val machineName: String,

    @SerializedName("price_per_hour")
    val pricePerHour: Int,

    @SerializedName("price_per_day")
    val pricePerDay: Int,

    @SerializedName("price_per_week")
    val pricePerWeek: Int,

    @SerializedName("price_per_month")
    val pricePerMonth: Int,

    @SerializedName("requester_location")
    val requesterLocation: String,

    @SerializedName("owner_phone")
    val ownerPhone: String,

    val status: String
)