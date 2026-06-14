package com.example.khetseva3.model

data class AddMachineRequest(
    val type: String,
    val model_name: String,
    val hp_range: Int? = null,
    val cutting_width: Double? = null,
    val working_width: Double? = null,
    val row_count: Int? = null,
    val price_per_hour: Int? = null,
    val price_per_day: Int? = null,
    val price_per_week: Int? = null,
    val price_per_month: Int? = null,
    val owner_name: String,
    val owner_phone: String,
    val location: String,
    val owner_email: String?,
    val image_url: String? = null
)