package com.andreramosdovale.seatsync.domain.repository

import com.andreramosdovale.seatsync.domain.model.UserAccount
import java.util.*

interface UserAccountRepository {
    fun save(userAccount: UserAccount): UserAccount
    fun findByEmail(email: String): UserAccount?
    fun existsByEmail(email: String): Boolean
    fun findById(userId: UUID): UserAccount?
}