package com.andreramosdovale.seatsync.domain.model

import com.andreramosdovale.seatsync.domain.model.vo.Email
import java.util.*

data class UserAccount(
    val userId: UUID,
    val fullName: String,
    val email: Email,
    val passwordHash: String,
    val roles: Set<RoleCode>
) {
    fun isAdmin(): Boolean {
        return roles.contains(RoleCode.ROLE_ADMIN)
    }

    fun isStaff(): Boolean {
        return roles.contains(RoleCode.ROLE_STAFF) || roles.contains(RoleCode.ROLE_ADMIN)
    }
}