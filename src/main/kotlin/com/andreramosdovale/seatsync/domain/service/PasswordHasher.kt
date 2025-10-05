package com.andreramosdovale.seatsync.domain.service

interface PasswordHasher {
    fun encode(raw: String): String
}