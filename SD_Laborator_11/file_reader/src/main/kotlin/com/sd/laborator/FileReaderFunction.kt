package com.sd.laborator

import io.micronaut.function.FunctionBean
import io.micronaut.function.executor.FunctionInitializer
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import jakarta.inject.Inject
import java.io.File

@FunctionBean("file_reader")
class FileReaderFunction: FunctionInitializer(), Supplier<EratosteneResponse> {
    @Inject
    private lateinit var client: PrimeCheckerClient

    private var LOG = LoggerFactory.getLogger(FileReaderFunction::class.java)

    override fun get(): EratosteneResponse {
        LOG.info(" Functia de citire din fisier a pornit")

        val numbersFromFile = try {
            // Căutăm fișierul direct în folderul src/main/resources
            val fileContent = this::class.java.classLoader.getResource("numere.txt")?.readText()
                ?: throw Exception("Fisierul nu a fost gasit in resources!")

            // Împărțim textul pe linii, ignorăm liniile goale și transformăm în Int
            fileContent.lines()
                .filter { it.isNotBlank() }
                .map { it.trim().toInt() }

        } catch (e: Exception) {
            LOG.error("Eroare la citirea fisierului: ${e.message}")
            val errResponse = EratosteneResponse()
            errResponse.setMessage("Eroare la citirea fisierului numere.txt")
            return errResponse
        }

        LOG.info("S-au citit numerele: $numbersFromFile")

        val request = EratosteneRequest()
        request.number = 100
        request.numbersToCheck = numbersFromFile

        LOG.info("Se trimit datele catre serviciul cu ciurul lui Eratostene...")

        val response = client.sendNumbersToCheck(request)

        LOG.info("S-a primit raspunsul!")

        return response
    }
}

fun main(args: Array<String>){
    val function = FileReaderFunction()
    function.run(args, {
            context -> function.get()})
}