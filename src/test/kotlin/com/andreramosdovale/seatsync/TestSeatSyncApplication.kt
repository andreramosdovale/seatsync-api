package com.andreramosdovale.seatsync

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<SeatSyncApplication>().with(TestcontainersConfiguration::class).run(*args)
}
