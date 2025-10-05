package com.andreramosdovale.seatsync.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterUserRequest(
    @field:NotBlank(message = "Full name cannot be blank")
    val fullName: String,

    @field:NotBlank
    @field:Email(message = "Email must be a valid format")
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String
)
