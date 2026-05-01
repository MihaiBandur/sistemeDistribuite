package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import java.io.File
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.nextUInt

@EnableBinding(Processor::class)
@SpringBootApplication
class FacturareMicroservice {

    private  val DB_PATH = "/Users/bandu/Documents/Anul_trei/Semestrul_doi/Sisteme_Distribuite/laboratoare_facute_de_mine/SD_Laborator_09/exemplul 2/DB_FOLDER/"
    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    fun emitereFactura(idComandaStr: String?): String? {
        if(idComandaStr == null || idComandaStr == "RESPINSA")  return  null

        println("Facturare: Emit factura pentru comanda cu ID $idComandaStr...")
        val nrFactura = abs(Random.nextInt())

        val comenzi = File(DB_PATH + "comenzi.txt").readLines()
        val liniaComenzii = comenzi.find {
            it.startsWith("$idComandaStr|")
        }
        if(liniaComenzii !=null){
            val detalii = liniaComenzii.split("|")
            val client = detalii[1]
            val produs = detalii[2]
            val cantitate = detalii[3]

            val inregistrareFactura = "FAC-$nrFactura|Comanda:$idComandaStr|$client|$produs|$cantitate buc.\n"
            File(DB_PATH+"facturi.txt").appendText(inregistrareFactura)
            println("Facturare: S-a emis si salvat factura FAC-$nrFactura.")
        }
        return idComandaStr
    }
}

fun main(args: Array<String>) {
    runApplication<FacturareMicroservice>(*args)
}