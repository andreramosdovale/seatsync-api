package com.andreramosdovale.seatsync.application.dto

data class RegisterUserCommand(
    val fullName: String,
    val email: String,
    val rawPassword: String
)