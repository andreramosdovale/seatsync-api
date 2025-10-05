package com.andreramosdovale.seatsync.infrastucture.persistence.repository

import com.andreramosdovale.seatsync.infrastucture.persistence.entity.UserAccountJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SpringDataUserAccountRepository : JpaRepository<UserAccountJpaEntity, UUID> {
    fun findByEmail(email: String): UserAccountJpaEntity?
    fun existsByEmail(email: String): Boolean
}