package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.messaging.Message
import org.springframework.web.client.RestTemplate

@EnableBinding(Sink::class)
@SpringBootApplication
class ReaprovizionareMicroservice{
    private  val dbUrl = "http://localhost:8080/api"
    private val restTemplate: RestTemplate = RestTemplate()

    @StreamListener(Sink.INPUT)
    fun procesareEvenimentNor(mesajEveniment: Message<String>){
        val produs = mesajEveniment.payload
        val tipEveniment = mesajEveniment.headers["tipEveniment"]


        println("S-a intrat in microservocul de reaprovizionare")
        if(tipEveniment == "LIPSA_STOC_CRITICA"){


            val cantitateGenerata = 500
            try {
                restTemplate.postForLocation(
                    "$dbUrl/stoc/aprovizionare?produs=$produs&cantitate=$cantitateGenerata",
                    null
                )
                println("PRODUCATOR: Am expediat $cantitateGenerata bucati inapoi spre Depozit!")
            }catch (e: Exception){
                println("PRODUCATOR: Eroare actualizare stoc: ${e.message}")
            }
        }
    }
}

fun main(Args: Array<String>){
    runApplication<ReaprovizionareMicroservice>(*Args)
}