package com.sd.laborator

import com.sd.laborator.services.BucatarService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class RestaurantApp

fun main(args: Array<String>) {
    val ctx: ApplicationContext = runApplication<RestaurantApp>(*args)
    val bucatar = ctx.getBean(BucatarService::class.java)
    println("Bucatar pornit cu ID: ${bucatar.bucatarId}")
}