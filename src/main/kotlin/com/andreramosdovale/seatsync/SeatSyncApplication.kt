package com.andreramosdovale.seatsync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SeatSyncApplication

fun main(args: Array<String>) {
	runApplication<SeatSyncApplication>(*args)
}
