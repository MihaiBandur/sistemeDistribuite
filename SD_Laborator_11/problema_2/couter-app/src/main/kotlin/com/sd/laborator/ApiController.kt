package com.sd.laborator
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Body
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

@Controller("/api")
@ExecuteOn(TaskExecutors.BLOCKING)
class ApiController(private val producer: ClickProducer) {

    @Post("/click")
    fun registerClick(@Body event: ClickEvent): String{
        producer.sendClickEvent(event)

        return "{\"status\": \"Event trimis in coada\"}"
    }
}