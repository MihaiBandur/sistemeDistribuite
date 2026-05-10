package com.sd.laborator

import io.micronaut.rabbitmq.annotation.Binding
import io.micronaut.rabbitmq.annotation.RabbitClient

@RabbitClient
interface ClickProducer {
    @Binding("click_queue")
    fun sendClickEvent(message: String)
}