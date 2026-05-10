package com.sd.laborator

import com.rabbitmq.client.Channel
import io.micronaut.rabbitmq.connect.ChannelInitializer
import jakarta.inject.Singleton
import java.io.IOException

@Singleton
class RabbitConfig : ChannelInitializer() {

    @Throws(IOException::class)
    override fun initialize(channel: Channel, name: String) {

        channel.queueDeclare("click_queue", true, false, false, null)
    }
}