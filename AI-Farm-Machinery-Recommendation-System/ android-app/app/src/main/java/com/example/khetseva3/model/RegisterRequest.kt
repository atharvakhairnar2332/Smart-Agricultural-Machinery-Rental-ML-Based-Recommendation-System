package com.example.khetseva3.model

data class RegisterRequest(
    val name: String,
    val phone: String,
    val password: String,
    val country: String,
    val state: String,
    val city: String,
    val email: String
)