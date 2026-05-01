package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer
import org.springframework.messaging.support.MessageBuilder
import java.io.File
import javax.validation.constraints.Null
import kotlin.random.Random

@EnableBinding(Processor::class)
@SpringBootApplication
class DepozitMicroservice {
    private val DB_PATH = "/Users/bandu/Documents/Anul_trei/Semestrul_doi/Sisteme_Distribuite/laboratoare_facute_de_mine/SD_Laborator_09/exemplul 2/DB_FOLDER/"

    private fun preiaDetaliiComanda(id: String): List<String>?{
        val fisierComenezi = File(DB_PATH + "comenzi.txt")
        if(!fisierComenezi.exists()){
            return null
        }
        val comenzi = fisierComenezi.readLines()

        val liniaComenzi = comenzi.find {
            it.startsWith("$id|")
        }
        return liniaComenzi?.split("|")
    }
    private fun acceptareComanda(identificator: Int): String {
            println("Comanda cu identificatorul $identificator a fost acceptata")

        val detalii = preiaDetaliiComanda(identificator.toString())
        if (detalii != null){
           val produs = detalii[2]
           val cantitate =detalii[3].toInt()
           return  pregatireColet(produs, cantitate)
        }
        return "EROARE_PREGATIRE"
    }

    private fun respingereComanda(identificator: Int): String {
        println("Comanda cu identificatorul $identificator a fost respinsa! Stoc insuficient.")
        return "RESPINSA"
    }

    private fun verificareStoc(produs: String, cantitate: Int): Boolean {
        val fisierStoc = File(DB_PATH + "stoc.txt")
        if(!fisierStoc.exists()) return  false

        val linii = fisierStoc.readLines()
        for (linie in linii){
            val (numeProdus, stocPtr) = linie.split("|")
            if(numeProdus == produs && stocPtr.toInt() >= cantitate){
                return true
            }
        }
        return false
    }

    private fun pregatireColet(produs: String, cantitate: Int): String {
        val fisireStoc = File(DB_PATH + "stoc.txt")
        if(fisireStoc.exists()){
            val linii = fisireStoc.readLines()

            val stocActualizat = linii.map { linie ->
                val (numeProdus, stocPtr) = linie.split("|")
                if(numeProdus == produs){
                    "$numeProdus|${stocPtr.toInt() - cantitate}"
                }else{
                    linie
                }
            }
            fisireStoc.writeText(stocActualizat.joinToString("\n") + "\n")
        }
        println("Produsul $produs in cantitate de $cantitate buc. este pregatit de livrare.")
        return "$produs|$cantitate"
    }

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    ///TODO - parametrul ar trebui sa fie doar numarul de inregistrare al comenzii si atat
    fun procesareComanda(idComandaStr: String?): String? {
        if(idComandaStr == null || idComandaStr == "RESPINSA") return null

        val identificatorComanda = idComandaStr.toIntOrNull() ?: return null
        println("Procesez comanda cu identificatorul $identificatorComanda...")

        val detalii = preiaDetaliiComanda(idComandaStr)
        if (detalii == null){
            println("Eroare: Comanda $identificatorComanda nu a fost gasita in DB.")
            return null
        }

        val produs = detalii[2]
        val cantitate = detalii[3].toInt()

        val rezultatProcesareComanda: String = if(verificareStoc(produs, cantitate)){
            acceptareComanda(identificatorComanda)
        }else{
            respingereComanda(identificatorComanda)
        }

        if(rezultatProcesareComanda == "RESPINSA")
            return "RESPINSA"

        return idComandaStr
    }
}

fun main(args: Array<String>) {
    runApplication<DepozitMicroservice>(*args)
}