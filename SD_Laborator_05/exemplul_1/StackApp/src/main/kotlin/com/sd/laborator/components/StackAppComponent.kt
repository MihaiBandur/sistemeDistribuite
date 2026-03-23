package com.sd.laborator.components

import com.sd.laborator.interfaces.CartesianProductOperation
import com.sd.laborator.interfaces.PrimeNumberGenerator
import com.sd.laborator.interfaces.UnionOperation
import com.sd.laborator.model.Stack
import com.sd.laborator.services.CartesianStepOneService
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StackAppComponent {
    private var A: Stack? = null;
    private var B: Stack? = null;

    @Autowired
    private lateinit var primeNumberGenerator: PrimeNumberGenerator

    @Autowired
    private lateinit var stepOneService: CartesianStepOneService

    @Autowired
    private lateinit var connectionFactoryComponent: RabbitMqConnectionFactoryComponent

    private lateinit var amqpTemplate: AmqpTemplate

    @Autowired
    fun initTemplate(){
        this.amqpTemplate = connectionFactoryComponent.rabbitTemplate()
    }

    @RabbitListener(queues = ["\${stackapp.rabbitmq.queue}"])
    fun receiveMessage(msg: String){
        val processed_msg = (msg.split(",").map {
            it.toInt().toChar()
        }).joinToString(separator = "")
        when(processed_msg){
            "compute" -> computeExpression()
            "regenerate_A"->sendMessage(regenerate_A())
            "regenerate_B"->sendMessage(regenerate_B())
        }
    }
    private fun generateStack(count: Int): Stack?{
        if (count < 1){
            return null
        }
        val X: MutableSet<Int> = mutableSetOf()
        while (X.count() < count)
            X.add(primeNumberGenerator.generatePrimeNumber())
        return Stack(X)
    }
    private fun computeExpression(){
        if(A ==  null)
            A = generateStack(20)
        if(B ==  null)
            B = generateStack(20)

        if(A!!.data.count() == B!!.data.count()){
            stepOneService.process(A!!.data, B!!.data)
        }else{
            sendMessage("compute~Error: A.count() != B.count()")
        }
    }

    fun sendMessage(msg: String) {
        println("message: \n$msg")
        this.amqpTemplate.convertAndSend(
            connectionFactoryComponent.getExchange(),
            connectionFactoryComponent.getRoutingKey(),
            msg
        )
    }

    private fun regenerate_A(): String{
        A = generateStack(20)
        return "A~" + A?.data.toString()
    }

    private fun regenerate_B(): String{
        B = generateStack(20)
        return "B~" + B?.data.toString()
    }

}
