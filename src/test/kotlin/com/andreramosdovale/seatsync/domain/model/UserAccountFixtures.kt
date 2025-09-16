package com.andreramosdovale.seatsync.domain.model

import com.andreramosdovale.seatsync.domain.model.vo.Email
import java.util.*

object UserAccountFixtures {

    fun customerUser(
        userId: UUID = UUID.randomUUID(),
        fullName: String = "Default Customer",
        email: Email = Email.of("customer@example.com"),
        passwordHash: String = "hash123"
    ): UserAccount {
        return UserAccount(
            userId = userId,
            fullName = fullName,
            email = email,
            passwordHash = passwordHash,
            roles = setOf(RoleCode.ROLE_CUSTOMER)
        )
    }

    fun staffUser(
        userId: UUID = UUID.randomUUID(),
        fullName: String = "Default Staff",
        email: Email = Email.of("staff@example.com"),
        passwordHash: String = "hash123"
    ): UserAccount {
        return UserAccount(
            userId = userId,
            fullName = fullName,
            email = email,
            passwordHash = passwordHash,
            roles = setOf(RoleCode.ROLE_STAFF, RoleCode.ROLE_CUSTOMER)
        )
    }

    fun adminUser(
        userId: UUID = UUID.randomUUID(),
        fullName: String = "Default Admin",
        email: Email = Email.of("admin@example.com"),
        passwordHash: String = "hash123"
    ): UserAccount {
        return UserAccount(
            userId = userId,
            fullName = fullName,
            email = email,
            passwordHash = passwordHash,
            roles = setOf(RoleCode.ROLE_ADMIN, RoleCode.ROLE_STAFF, RoleCode.ROLE_CUSTOMER)
        )
    }
}