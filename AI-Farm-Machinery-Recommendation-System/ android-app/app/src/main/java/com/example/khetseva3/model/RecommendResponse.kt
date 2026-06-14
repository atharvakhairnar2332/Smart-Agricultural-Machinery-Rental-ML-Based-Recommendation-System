package com.example.khetseva3.model

import com.google.gson.annotations.SerializedName

data class RecommendResponse(

    @SerializedName("prediction")
    val prediction: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("machines")
    val machines: List<Machine>
)