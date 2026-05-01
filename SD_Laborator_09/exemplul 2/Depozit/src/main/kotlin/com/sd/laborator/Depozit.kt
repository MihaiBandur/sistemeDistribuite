package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import org.springframework.web.client.RestTemplate

@EnableBinding(Processor::class)
@SpringBootApplication
class DepozitMicroservice {

    private val dbUrl = "http://localhost:8080/api"
    private val restTemplate = RestTemplate()

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    fun procesareComanda(idComandaStr: String?): String? {
        if (idComandaStr == null || idComandaStr == "RESPINSA") return null
        println("Depozit: Procesez comanda $idComandaStr...")

        try {

            val detaliiComanda = restTemplate.getForObject("$dbUrl/comenzi/$idComandaStr", String::class.java)
            if (detaliiComanda.isNullOrEmpty()) {
                println("Depozit: Comanda $idComandaStr nu a fost gasita in DB.")
                return null
            }

            val detalii = detaliiComanda.split("|")
            val produs = detalii[1
            val cantitate = detalii[2].toInt()

            val stocCurent = restTemplate.getForObject("$dbUrl/stoc?produs=$produs", Int::class.java) ?: 0

            if (stocCurent >= cantitate) {
                restTemplate.postForLocation("$dbUrl/stoc/extrage?produs=$produs&cantitate=$cantitate", null)
                println("Depozit: Produsul $produs ($cantitate buc) e pregatit. Comanda $idComandaStr acceptata.")
                return idComandaStr
            } else {
                println("Depozit: Stoc insuficient pentru $produs. Comanda $idComandaStr respinsa!")
                return "RESPINSA"
            }
        } catch (e: Exception) {
            println("Depozit: Eroare comunicare DB: ${e.message}")
            return "RESPINSA"
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DepozitMicroservice>(*args)
}