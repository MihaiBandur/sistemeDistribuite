package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.client.RestTemplate

data class ComandaAmanataRequest(val numeClient: String, val produs: String, val cantitate: Int)

interface  CanaleDepozit{
    @Input("input") fun input(): SubscribableChannel
    @Output("output") fun output(): MessageChannel
    @Output("evenimentAprovizionare") fun evenimentAprovizionare(): MessageChannel
}

@EnableBinding(CanaleDepozit::class)
@SpringBootApplication
class DepozitMicroservice(private  val canale: CanaleDepozit) {

    private val dbUrl = "http://localhost:8080/api"
    private val restTemplate = RestTemplate()

    @StreamListener("input")
    @SendTo("output")
    fun procesareComanda(idComandaStr: String?): String? {
        if (idComandaStr == null || idComandaStr == "RESPINSA") return null

        try {
            val detaliiComanda =
                restTemplate.getForObject("$dbUrl/comenzi/$idComandaStr", String::class.java) ?: return null
            val detalii = detaliiComanda.split("|")
            val numeClient = detalii[0]
            val produs = detalii[1]
            val cantitate = detalii[2].toInt()

            val stocCurent = restTemplate.getForObject("$dbUrl/stoc?produs=$produs", Int::class.java) ?: 0

            if (stocCurent >= cantitate) {
                restTemplate.postForLocation("$dbUrl/stoc/extrage?produs=$produs&cantitate=$cantitate", null)
                return idComandaStr
            } else {
                restTemplate.postForLocation(
                    "$dbUrl/comenzi-amanate",
                    ComandaAmanataRequest(numeClient, produs, cantitate)
                )

                val trebuieAprovizionare = restTemplate.getForObject(
                    "$dbUrl/stoc/verificare-aprovizionare?produs=$produs",
                    Boolean::class.java
                ) ?: false

                if (trebuieAprovizionare) {
                    println("Depozit: Emit EVENIMENT NOR pentru reaprovizionarea produsului: $produs")

                    val cloudEvent = MessageBuilder
                        .withPayload(produs)
                        .setHeader("tipEveniment", "LIPSA_STOC_CRITICA") // Metadate specifice evenimentelor
                        .build()

                    ca  nale.evenimentAprovizionare().send(cloudEvent)
                }

                return null
            }
        } catch (e: Exception) {
            return null
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DepozitMicroservice>(*args)
}