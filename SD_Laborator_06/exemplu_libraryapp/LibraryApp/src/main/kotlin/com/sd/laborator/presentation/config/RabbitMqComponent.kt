package com.sd.laborator.presentation.config

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
@Component
class RabbitMqComponent {
    @Value("\${spring.rabbitmq.host}")
    private  lateinit var host: String
    @Value("\${spring.rabbitmq.port}")
    private var port: Int = 0
    @Value("\${spring.rabbitmq.username}")
    private lateinit var username: String
    @Value("\${spring.rabbitmq.password}")
    private lateinit var password: String
    @Value("\${libraryapp.rabbitmq.exchange}")
    private lateinit var exchange: String
    @Value("\${libraryapp.rabbitmq.routingkey}")
    private  lateinit var routingkey: String

    fun getExchange(): String = this.exchange

    fun getRountingKey(): String = this.routingkey

    private  fun connectionFactory(): ConnectionFactory{
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.host = this.host
        connectionFactory.port = this.port
        connectionFactory.username = this.username
        connectionFactory.setPassword(this.password)
        return connectionFactory

    }
    @Bean
    fun rabbitTemplate(): RabbitTemplate = RabbitTemplate(connectionFactory())

}