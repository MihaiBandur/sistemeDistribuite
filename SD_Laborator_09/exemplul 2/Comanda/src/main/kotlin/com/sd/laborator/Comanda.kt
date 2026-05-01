package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

data class ClientRequest(val nume: String, val adresa: String)

@EnableBinding(Processor::class)
@SpringBootApplication
class ComandaMicroservice {

    private val dbUrl = "http://localhost:8080/api"
    private val restTemplate = RestTemplate()

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    fun preluareComanda(comanda: String?): String? {
        if (comanda == null) return null

        val (identitateClient, produsComandat, cantitate, adresaLivrare) = comanda.split("|")
        println("Comanda: Am preluat comanda de la clientul: $identitateClient")

        try {
            val clientNou = ClientRequest(nume = identitateClient, adresa = adresaLivrare)
            restTemplate.postForObject("$dbUrl/clienti", clientNou, String::class.java)
            println("Comanda: Clientul $identitateClient a fost salvat in DB.")
        } catch (e: Exception) {
            println("Comanda: Eroare la salvarea clientului in DB: ${e.message}")
        }

        val idComanda = Random.nextInt(1, 100000)
        try {
            restTemplate.postForLocation("$dbUrl/comenzi/$idComanda", comanda)
            println("Comanda: Am salvat comanda in DB si trimit pe flux ID-ul: $idComanda")
            return idComanda.toString()
        } catch (e: Exception) {
            println("Comanda: Eroare la salvarea comenzii in DB: ${e.message}")
            return null
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ComandaMicroservice>(*args)
}