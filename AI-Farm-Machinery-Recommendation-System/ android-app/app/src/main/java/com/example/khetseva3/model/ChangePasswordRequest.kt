package com.example.khetseva3.model

data class ChangePasswordRequest(

    val phone: String,

    val current_password: String,

    val new_password: String
)