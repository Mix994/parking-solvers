package com.parkingsolvers.smartpark

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmartParkApiApplication

fun main(args: Array<String>) {
    runApplication<SmartParkApiApplication>(*args)
}
