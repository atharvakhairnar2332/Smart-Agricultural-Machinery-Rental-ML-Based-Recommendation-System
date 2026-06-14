package com.example.khetseva3.model

data class UpdateProfileRequest(

    val phone: String,

    val name: String,

    val email: String,

    val country: String,

    val state: String,

    val city: String
)