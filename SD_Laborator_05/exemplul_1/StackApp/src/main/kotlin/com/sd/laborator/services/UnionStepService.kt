package com.sd.laborator.services

import com.sd.laborator.components.RabbitMqConnectionFactoryComponent
import com.sd.laborator.interfaces.UnionOperation
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UnionStepService(
    private  val unionService: UnionService,
    private val connectionFactoryComponent: RabbitMqConnectionFactoryComponent
) {
    private lateinit var amqpTemplate: AmqpTemplate

    @Autowired
    fun initTemplate(){
        this.amqpTemplate = connectionFactoryComponent.rabbitTemplate()
    }

    fun process(aXb: Set<Pair<Int, Int>>, bXb: Set<Pair<Int, Int>>, A: Set<Int>, B: Set<Int>){
        val result = unionService.executeOperation(aXb, bXb)

        val msg = "compute~{\"A\": \"$A\", \"B\": \"$B\", \"result\": \"$result\"}"
        println("Lantul a fost completat. Se trimite mesajul: \n$msg")

        this.amqpTemplate.convertAndSend(
            connectionFactoryComponent.getExchange(),
            connectionFactoryComponent.getRoutingKey(),
            msg
        )
    }


}