package com.sd.laborator
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

@Controller("/api")
@ExecuteOn(TaskExecutors.BLOCKING)
class ApiController(private val producer: ClickProducer) {

    @Post("/click")
    fun registerClick(): String{
        producer.sendClickEvent("Click nou detectat")

        return "{\"status\": \"Event trimis in coada\"}"
    }
}