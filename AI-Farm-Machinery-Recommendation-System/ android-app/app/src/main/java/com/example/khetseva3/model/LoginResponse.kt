package com.example.khetseva3.model

data class LoginResponse(
    val message: String,
    val name: String,
    val phone: String,
    val email: String,
    val country: String,
    val state: String,
    val city: String,

)