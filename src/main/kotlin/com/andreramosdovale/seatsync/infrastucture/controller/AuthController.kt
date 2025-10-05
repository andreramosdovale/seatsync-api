package com.andreramosdovale.seatsync.infrastucture.controller

import com.andreramosdovale.seatsync.application.dto.RegisterUserCommand
import com.andreramosdovale.seatsync.application.dto.RegisterUserRequest
import com.andreramosdovale.seatsync.application.useCase.RegisterUserUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase
) {

    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<Any> {
        val command = RegisterUserCommand(
            fullName = request.fullName,
            email = request.email,
            rawPassword = request.password
        )

        val newUserId = registerUserUseCase.execute(command)

        return ResponseEntity.created(URI.create("/api/users/$newUserId")).build()
    }
}