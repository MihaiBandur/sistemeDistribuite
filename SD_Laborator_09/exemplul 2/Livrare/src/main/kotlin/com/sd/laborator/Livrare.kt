package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.web.client.RestTemplate

@EnableBinding(Sink::class)
@SpringBootApplication
class LivrareMicroservice {

    private val dbUrl = "http://localhost:8080/api"
    private val restTemplate = RestTemplate()

    @StreamListener(Sink.INPUT)
    fun expediereComanda(idComandaStr: String?) {
        if (idComandaStr == null || idComandaStr == "RESPINSA") return

        try {
            val detaliiComanda = restTemplate.getForObject("$dbUrl/comenzi/$idComandaStr", String::class.java)
            if (!detaliiComanda.isNullOrEmpty()) {
                val detalii = detaliiComanda.split("|")
                val client = detalii[0]
                val adresa = detalii[3]

                val detaliiLivrare = "LIVRAT | $idComandaStr | $client | $adresa"
                restTemplate.postForLocation("$dbUrl/livrari", detaliiLivrare)
                println("Livrare: Comanda $idComandaStr a plecat spre $client la adresa: $adresa.")
            }
        } catch (e: Exception) {
            println("Livrare: Eroare - ${e.message}")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LivrareMicroservice>(*args)
}