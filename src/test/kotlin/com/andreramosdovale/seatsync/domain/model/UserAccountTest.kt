package com.andreramosdovale.seatsync.domain.model

import com.andreramosdovale.seatsync.domain.model.vo.Email
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserAccountTest {

    @Test
    fun `isAdmin should return true when user has ADMIN role`() {
        val adminUser = UserAccountFixtures.adminUser()

        val result = adminUser.isAdmin()

        assertThat(result).isTrue()
    }

    @Test
    fun `isAdmin should return false when user does not have ADMIN role`() {
        val customerUser = UserAccountFixtures.customerUser()
        val staffUser = UserAccountFixtures.staffUser()

        val customerResult = customerUser.isAdmin()
        val staffResult = staffUser.isAdmin()

        assertThat(customerResult).isFalse()
        assertThat(staffResult).isFalse()
    }

    @Test
    fun `isStaff should return true for STAFF and ADMIN users`() {
        val adminUser = UserAccountFixtures.adminUser()
        val staffUser = UserAccountFixtures.staffUser()
        val customerUser = UserAccountFixtures.customerUser()

        assertThat(adminUser.isStaff()).isTrue()
        assertThat(staffUser.isStaff()).isTrue()
        assertThat(customerUser.isStaff()).isFalse()
    }

    @Test
    fun `can create a customer with a specific email for testing`() {
        val customEmail = Email.of("special-test-case@example.com")
        val customerUser = UserAccountFixtures.customerUser(email = customEmail)

        assertThat(customerUser.email).isEqualTo(customEmail)
        assertThat(customerUser.roles).contains(RoleCode.ROLE_CUSTOMER)
    }
}