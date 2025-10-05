package com.andreramosdovale.seatsync.infrastucture.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "role")
class RoleJpaEntity(
    @Id
    var name: String,

    var description: String? = null
)