package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import org.springframework.messaging.support.MessageBuilder
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.random.Random

@EnableBinding(Processor::class)
@SpringBootApplication
class ComandaMicroservice {

    private val DB_PATH = "/Users/bandu/Documents/Anul_trei/Semestrul_doi/Sisteme_Distribuite/laboratoare_facute_de_mine/SD_Laborator_09/exemplul 2/DB_FOLDER/"

    private fun pregatireComanda(comandaBruta: String): Int {
        val idComanda = Random.nextInt(1, 100)
        val fisierComenzi = File(DB_PATH + "comenzi.txt" )
        fisierComenzi.appendText("$idComanda|$comandaBruta\n")
        return idComanda
    }

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    fun preluareComanda(comanda: String?): String {
        val (identitateClient, produsComandat, cantitate, adresaLivrare) = comanda!!.split("|")
        println("Comanda: Am salvat comanda: $identitateClient | $produsComandat | $cantitate | $adresaLivrare")

        val idComanda = pregatireComanda(comanda)

        println("Comanda: Am salvat comanda in DB si trimit pe flux ID-ul: $idComanda")

        return idComanda.toString()
    }
}

fun main(args: Array<String>) {
    runApplication<ComandaMicroservice>(*args)
}