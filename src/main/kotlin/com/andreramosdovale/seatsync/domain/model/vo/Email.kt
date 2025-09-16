package com.andreramosdovale.seatsync.domain.model.vo

import java.io.Serializable

@JvmInline
value class Email(val value: String) : Serializable {

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        )

        fun of(email: String): Email {
            return Email(email)
        }
    }

    init {
        require(EMAIL_REGEX.matches(value)) {
            "Invalid email format: $value"
        }
    }

    override fun toString(): String = value
}