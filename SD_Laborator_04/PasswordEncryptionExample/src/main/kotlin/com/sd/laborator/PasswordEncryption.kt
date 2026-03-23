package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PasswordEncryption

fun main(args: Array<String>) {
    runApplication<PasswordEncryption>(*args)
}