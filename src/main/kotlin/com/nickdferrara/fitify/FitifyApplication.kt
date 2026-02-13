package com.nickdferrara.fitify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FitifyApplication

fun main(args: Array<String>) {
    runApplication<FitifyApplication>(*args)
}
