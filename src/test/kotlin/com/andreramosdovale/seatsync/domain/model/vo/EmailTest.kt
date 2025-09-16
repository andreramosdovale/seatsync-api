package com.andreramosdovale.seatsync.domain.model.vo

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class EmailTest {

    @Test
    fun `should create Email object for a valid email string`() {
        val validEmailString = "test@example.com"

        val email = Email.of(validEmailString)

        assertThat(email).isNotNull()
        assertThat(email.value).isEqualTo(validEmailString)
        assertThat(email.toString()).isEqualTo(validEmailString)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "user.name@domain.com",
            "user+tag@gmail.com",
            "user@domain.co.uk",
            "12345@numeric-domain.io",
            "firstname.lastname@sub.domain.org"
        ]
    )
    fun `should accept various valid email formats`(validEmail: String) {
        val email = Email.of(validEmail)

        assertThat(email.value).isEqualTo(validEmail)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "plainaddress",
            "test@.com",
            "@missing-user.com",
            "user@missing-domain",
            "user with spaces@test.com",
            "user@domain..com",
            ""
        ]
    )
    fun `should throw IllegalArgumentException for invalid email formats`(invalidEmail: String) {
        assertThatThrownBy {
            Email.of(invalidEmail)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Invalid email format")
    }

    @Test
    fun `should throw exception for null input in factory`() {
        val nullString = "null"
        assertThatThrownBy {
            Email.of(nullString)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}