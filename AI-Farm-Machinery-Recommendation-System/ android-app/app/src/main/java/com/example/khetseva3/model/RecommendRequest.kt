package com.example.khetseva3.model
import com.google.gson.annotations.SerializedName
import com.example.khetseva3.model.Inputs
data class RecommendRequest(

    @SerializedName("type")
    val type: String,

    @SerializedName("pricing_type")
    val pricing_type: String,

    @SerializedName("inputs")
    val inputs: Inputs
)