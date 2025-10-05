package com.andreramosdovale.seatsync.infrastucture.security

import com.andreramosdovale.seatsync.domain.service.PasswordHasher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher(
    private val passwordEncoder: PasswordEncoder
) : PasswordHasher {

    override fun encode(raw: String): String {
        return passwordEncoder.encode(raw)
    }
}