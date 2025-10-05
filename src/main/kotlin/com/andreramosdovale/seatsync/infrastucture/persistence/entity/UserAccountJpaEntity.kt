package com.andreramosdovale.seatsync.infrastucture.persistence.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "user_account")
class UserAccountJpaEntity(
    @Id
    var userId: UUID,

    var fullName: String,

    @Column(unique = true)
    var email: String,

    var passwordHash: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_account_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_name")]
    )
    var roles: MutableSet<RoleJpaEntity>
)