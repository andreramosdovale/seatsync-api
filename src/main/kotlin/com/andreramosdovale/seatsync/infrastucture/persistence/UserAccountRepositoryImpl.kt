package com.andreramosdovale.seatsync.infrastucture.persistence

import com.andreramosdovale.seatsync.domain.model.UserAccount
import com.andreramosdovale.seatsync.domain.repository.UserAccountRepository
import com.andreramosdovale.seatsync.infrastucture.persistence.mapper.toDomainModel
import com.andreramosdovale.seatsync.infrastucture.persistence.mapper.toJpaEntity
import com.andreramosdovale.seatsync.infrastucture.persistence.repository.SpringDataUserAccountRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserAccountRepositoryImpl(
    private val jpaRepository: SpringDataUserAccountRepository
) : UserAccountRepository {

    override fun save(userAccount: UserAccount): UserAccount {
        val jpaEntity = userAccount.toJpaEntity()
        val savedEntity = jpaRepository.save(jpaEntity)

        return savedEntity.toDomainModel()
    }

    override fun findByEmail(email: String): UserAccount? {
        val jpaEntity = jpaRepository.findByEmail(email)

        return jpaEntity?.toDomainModel()
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

    override fun findById(userId: UUID): UserAccount? {
        return jpaRepository.findById(userId).map { it.toDomainModel() }.orElse(null)
    }
}