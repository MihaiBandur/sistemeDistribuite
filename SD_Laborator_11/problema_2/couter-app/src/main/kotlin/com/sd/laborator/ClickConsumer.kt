package com.sd.laborator

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import org.slf4j.LoggerFactory

@RabbitListener
class ClickConsumer(private  val repository: CounterRepository) {
    private val LOG = LoggerFactory.getLogger(ClickConsumer::class.java)

    @Queue("click_queue")
    fun processClickEvent(message: String){
        LOG.info("Functia a preluat mesajul din coada: $message")

        val counter = repository.findById(1L).orElse(ClickCounter(1L, 0))

        counter.clicks +=1

        repository.update(counter)

        LOG.info("MySQL actualizat! Total click-uri: ${counter.clicks}")
    }
}