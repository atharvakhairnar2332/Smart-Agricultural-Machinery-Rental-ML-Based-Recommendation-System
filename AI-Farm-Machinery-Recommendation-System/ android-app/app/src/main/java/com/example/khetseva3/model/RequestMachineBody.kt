package com.example.khetseva3.model

data class RequestMachineBody(

    val machine_id: Int,

    val requester_name: String,

    val requester_phone: String,

    val requester_email: String,

    val requester_location: String,

    val owner_phone: String
)