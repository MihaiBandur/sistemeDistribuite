package com.sd.laborator.components

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class RabbitMqConnectionFactoryComponent {

    @Value("\${spring.rabbitmq.host}")
    private lateinit var host: String

    @Value("\${spring.rabbitmq.port}")
    private val port: Int = 0

    @Value("\${spring.rabbitmq.username}")
    private lateinit var username: String

    @Value("\${spring.rabbitmq.password}")
    private lateinit var password: String

    @Value("\${restaurant.rabbitmq.exchange}")
    lateinit var exchange: String

    @Value("\${restaurant.rabbitmq.routingkey.orders}")
    lateinit var ordersRoutingKey: String

    @Value("\${restaurant.rabbitmq.routingkey.responses}")
    lateinit var responsesRoutingKey: String

    @Value("\${restaurant.rabbitmq.queue.orders}")
    lateinit var ordersQueue: String

    @Value("\${restaurant.rabbitmq.queue.responses}")
    lateinit var responsesQueue: String

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val factory = CachingConnectionFactory()
        factory.host = host
        factory.port = port
        factory.username = username
        factory.setPassword(password)
        return factory
    }

    @Bean
    fun rabbitTemplate(): RabbitTemplate = RabbitTemplate(connectionFactory())

    @Bean
    fun directExchange(): DirectExchange = DirectExchange(exchange)

    @Bean
    fun ordersQueueBean(): Queue = Queue(ordersQueue, true)

    @Bean
    fun responsesQueueBean(): Queue = Queue(responsesQueue, true)

    @Bean
    fun ordersBinding(): Binding =
        BindingBuilder.bind(ordersQueueBean()).to(directExchange()).with(ordersRoutingKey)

    @Bean
    fun responsesBinding(): Binding =
        BindingBuilder.bind(responsesQueueBean()).to(directExchange()).with(responsesRoutingKey)
}