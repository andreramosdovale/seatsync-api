package com.andreramosdovale.seatsync.application.useCase

import com.andreramosdovale.seatsync.application.dto.RegisterUserCommand
import com.andreramosdovale.seatsync.domain.service.PasswordHasher
import com.andreramosdovale.seatsync.domain.model.RoleCode
import com.andreramosdovale.seatsync.domain.model.UserAccount
import com.andreramosdovale.seatsync.domain.model.vo.Email
import com.andreramosdovale.seatsync.domain.repository.UserAccountRepository
import java.util.*

class RegisterUserUseCase(
    private val userAccountRepository: UserAccountRepository,
    private val passwordHasher: PasswordHasher
) {
    fun execute(command: RegisterUserCommand): UUID {
        val emailVO = Email.of(command.email)

        if (userAccountRepository.existsByEmail(emailVO.value)) {
            throw IllegalStateException("Email already registered")
        }

        val hashedPassword = passwordHasher.encode(command.rawPassword)

        val newUser = UserAccount(
            userId = UUID.randomUUID(),
            fullName = command.fullName,
            email = emailVO,
            passwordHash = hashedPassword,
            roles = setOf(RoleCode.ROLE_CUSTOMER)
        )

        userAccountRepository.save(newUser)

        return newUser.userId
    }
}