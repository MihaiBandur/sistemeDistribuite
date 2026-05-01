package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import java.io.File

@EnableBinding(Sink::class)
@SpringBootApplication
class LivrareMicroservice {

    private  val DB_PATH = "/Users/bandu/Documents/Anul_trei/Semestrul_doi/Sisteme_Distribuite/laboratoare_facute_de_mine/SD_Laborator_09/exemplul 2/DB_FOLDER/"
    @StreamListener(Sink.INPUT)

    fun expediereComanda(idComandaStr: String?) {
        if(idComandaStr == null) return

        val comenzi = File(DB_PATH + "comenzi.txt").readLines()
        val liniaComenzii = comenzi.find {
            it.startsWith("$idComandaStr|")
        }
        if(liniaComenzii !=null){
            val detalii = liniaComenzii.split("|")
            val client = detalii[1]
            val adresa = detalii[4]
            println("Livrare: Comanda $idComandaStr a plecat spre clientul $client la adresa: $adresa.")
            File(DB_PATH + "livrari.txt").appendText("LIVRAT|$idComandaStr|$client|$adresa\n")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LivrareMicroservice>(*args)
}