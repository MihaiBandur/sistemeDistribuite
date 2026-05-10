package com.sd.laborator

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import org.slf4j.LoggerFactory

@RabbitListener
class ClickConsumer(private  val repository: ButtonStatRepository) {
    private val LOG = LoggerFactory.getLogger(ClickConsumer::class.java)

    @Queue("click_queue")
    fun processClickEvent(event: ClickEvent){
        LOG.info("Functia a preluat mesajul din coada: ${event.buttonName}")

        val existingStat = repository.findByButtonName(event.buttonName)

        if(existingStat.isPresent){
            val stat = existingStat.get()
            stat.clicks += 1
            repository.update(stat)
            LOG.info("S-a actuazlizat contorul butonul ${stat.buttonName} fiind apasat de ${stat.clicks}")
        }
        else{
            val newStat = ButtonStat(buttonName = event.buttonName, clicks = 1)
            repository.save(newStat)
            LOG.info("Butonul ${event.buttonName} a fost creat cu succes")
        }

    }
}