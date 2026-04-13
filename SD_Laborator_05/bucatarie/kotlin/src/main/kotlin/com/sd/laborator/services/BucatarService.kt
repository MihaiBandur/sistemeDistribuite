package com.sd.laborator.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.laborator.components.RabbitMqConnectionFactoryComponent
import com.sd.laborator.model.Order
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

@Service
class BucatarService {

    val bucatarId: String = generateBucatarId()

    private val activeOrders: MutableMap<Int, Order> = mutableMapOf()

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var rabbitMqConfig: RabbitMqConnectionFactoryComponent

    private val objectMapper = ObjectMapper()

    companion object {
        fun generateBucatarId(): String {
            val digits = (10..99).random()
            val letters1 = (1..2).map { ('A'..'Z').random() }.joinToString("")
            val digits2 = (10..99).random()
            val letters2 = (1..2).map { ('A'..'Z').random() }.joinToString("")
            return "Bucatar$digits$letters1${digits2}$letters2"
        }
    }

    @RabbitListener(queues = ["\${restaurant.rabbitmq.queue.orders}"])
    fun receiveOrder(message: ByteArray) {
        val jsonString = String(message, Charsets.UTF_8)
        println("[$bucatarId] Comanda primita: $jsonString")

        try {
            val order: Order = objectMapper.readValue(jsonString)

            if (activeOrders.containsKey(order.orderId)) {
                println("[$bucatarId] Comanda ${order.orderId} este deja in lucru!")
                sendResponse(order.copy(
                    bucatarId = bucatarId,
                    status = "REJECTED - Slot ocupat"
                ))
                return
            }

            val acceptedOrder = order.copy(bucatarId = bucatarId, status = "IN_PROGRESS")
            activeOrders[order.orderId] = acceptedOrder

            println("[$bucatarId] Prepar comanda ${order.orderId} de la ${order.chelnerId}...")

            val prepTime = Random.nextLong(2000, 5001)
            Thread.sleep(prepTime)

            val doneOrder = acceptedOrder.copy(status = "DONE")
            activeOrders.remove(order.orderId)

            println("[$bucatarId] Comanda ${order.orderId} gata in ${prepTime}ms!")
            sendResponse(doneOrder)

        } catch (e: Exception) {
            println("[$bucatarId] Eroare: ${e.message}")
        }
    }


    fun sendResponse(order: Order) {
        val responseJson = objectMapper.writeValueAsString(order)
        println("[$bucatarId] Trimit raspuns: $responseJson")
        rabbitTemplate.convertAndSend(
            rabbitMqConfig.exchange,
            rabbitMqConfig.responsesRoutingKey,
            responseJson.toByteArray(charset = Charsets.UTF_8)
        )
    }
}