package com.andreramosdovale.seatsync.infrastucture.config

import com.andreramosdovale.seatsync.application.useCase.RegisterUserUseCase
import com.andreramosdovale.seatsync.domain.repository.UserAccountRepository
import com.andreramosdovale.seatsync.domain.service.PasswordHasher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {

    @Bean
    fun registerUserUseCase(
        userAccountRepository: UserAccountRepository,
        passwordHasher: PasswordHasher
    ): RegisterUserUseCase {
        return RegisterUserUseCase(userAccountRepository, passwordHasher)
    }
}