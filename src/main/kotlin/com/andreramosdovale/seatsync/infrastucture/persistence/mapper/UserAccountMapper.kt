package com.andreramosdovale.seatsync.infrastucture.persistence.mapper

import com.andreramosdovale.seatsync.domain.model.RoleCode
import com.andreramosdovale.seatsync.domain.model.UserAccount
import com.andreramosdovale.seatsync.domain.model.vo.Email
import com.andreramosdovale.seatsync.infrastucture.persistence.entity.RoleJpaEntity
import com.andreramosdovale.seatsync.infrastucture.persistence.entity.UserAccountJpaEntity

fun UserAccount.toJpaEntity(): UserAccountJpaEntity {
    return UserAccountJpaEntity(
        userId = this.userId,
        fullName = this.fullName,
        email = this.email.value,
        passwordHash = this.passwordHash,
        roles = this.roles.map { RoleJpaEntity(name = it.name) }.toMutableSet()
    )
}

fun UserAccountJpaEntity.toDomainModel(): UserAccount {
    return UserAccount(
        userId = this.userId,
        fullName = this.fullName,
        email = Email.of(this.email),
        passwordHash = this.passwordHash,
        roles = this.roles.map { RoleCode.valueOf(it.name) }.toSet()
    )
}