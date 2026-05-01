package com.sd.laborator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@EnableBinding(Source::class)
@SpringBootApplication
@RestController
class ClientMicroservice {

    @Autowired
    lateinit var source: Source


    @PostMapping("/api/client")
    fun primesteComandaFlask(@RequestBody mesajComanda: String): String {
        println("ClientMicroservice a primit comanda din Flask: $mesajComanda")

        val trimisCuSucces = source.output().send(MessageBuilder.withPayload(mesajComanda).build())

        return if (trimisCuSucces) {
            "Comanda a intrat pe fluxul nostru de date cu succes!"
        } else {
            "Eroare la trimiterea pe flux."
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ClientMicroservice>(*args)
}