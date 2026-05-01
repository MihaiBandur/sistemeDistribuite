package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import org.springframework.web.client.RestTemplate
import kotlin.math.abs
import kotlin.random.Random

@EnableBinding(Processor::class)
@SpringBootApplication
class FacturareMicroservice {

    private val dbUrl = "http://localhost:8080/api"
    private val restTemplate = RestTemplate()

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    fun emitereFactura(idComandaStr: String?): String? {
        if (idComandaStr == null || idComandaStr == "RESPINSA") return null

        try {
            val detaliiComanda = restTemplate.getForObject("$dbUrl/comenzi/$idComandaStr", String::class.java)
            if (!detaliiComanda.isNullOrEmpty()) {
                val detalii = detaliiComanda.split("|")
                val client = detalii[0]
                val produs = detalii[1]
                val cantitate = detalii[2]

                val nrFactura = abs(Random.nextInt())
                val detaliiFactura = "FAC-$nrFactura | Comanda: $idComandaStr | $client | $produs | $cantitate buc."

                restTemplate.postForLocation("$dbUrl/facturi", detaliiFactura)
                println("Facturare: S-a emis factura FAC-$nrFactura pentru comanda $idComandaStr.")
            }
        } catch (e: Exception) {
            println("Facturare: Eroare - ${e.message}")
        }
        return idComandaStr
    }
}

fun main(args: Array<String>) {
    runApplication<FacturareMicroservice>(*args)
}