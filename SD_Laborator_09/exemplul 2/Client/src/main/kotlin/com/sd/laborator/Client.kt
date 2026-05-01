package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.io.File
import kotlin.random.Random

@EnableBinding(Source::class)
@SpringBootApplication
open class ClientMicroservice {


    private val DB_PATH = "/Users/bandu/Documents/Anul_trei/Semestrul_doi/Sisteme_Distribuite/laboratoare_facute_de_mine/SD_Laborator_09/exemplul 2/DB_FOLDER/"

    private  fun citesteDinFisier(numeFisier: String): List<String>{
        val fisier = File(DB_PATH + numeFisier);
        return  if(fisier.exists()){
            fisier.readLines().filter { it.isNotBlank() }
        }else{
            println("ATENTIE: Fisierul ${fisier.absolutePath} nu exista!")
            emptyList()
        }
    }

    @Bean
    @InboundChannelAdapter(value = Source.OUTPUT, poller = [Poller(fixedDelay = "10000", maxMessagesPerPoll = "1")])
    open fun comandaProdus(): () -> Message<String>? {
        return {
            val listaProduse = citesteDinFisier("produse.txt")
            val listaClienti = citesteDinFisier("clienti.txt")

            if(listaProduse.isNotEmpty() && listaClienti.isNotEmpty()){
                val produsComandat = listaProduse.random()

                val dataClient = listaClienti.random().split("|")
                val identitateClient = dataClient[0];
                val adresaLivrare = if(dataClient.size > 1) dataClient[1] else "Adresa Necunoscuta"

                val cantitate: Int = Random.nextInt(1, 100)

                val mesaj = "$identitateClient|$produsComandat|$cantitate|$adresaLivrare"
                println("ClientMicroservice a generat comanda: $mesaj")
                MessageBuilder.withPayload(mesaj).build()
            }else{
                println("ClientMicroservice: Nu se poate genera comanda. Fisierele sunt goale sau lipsesc.")
                null
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ClientMicroservice>(*args)
}