package com.sd.laborator

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

data class SubscriberStatus(
    val port: Int,
    var lastSeen: Long = System.currentTimeMillis(),   // timestamp ultimul raspuns
    var totalResponses: Int = 0,
    var missedHeartbeats: Int = 0,
    var alive: Boolean = true
)

class HeartbeatMicroservice {
    private lateinit var messageManagerSocket: Socket

    private val statusMutex: Mutex = Mutex()
    private val subscriberStatuses = mutableMapOf<Int, SubscriberStatus>()

    companion object{
        val MESSAGE_MANAGER_HOST =
            System.getenv("MESSAGE_MANAGER_HOST") ?: "localhost"
        const val MESSAGE_MANAGER_PORT = 1500

        val HEARTBEAT_INTERVAL_MS =
            System.getenv("HEARTBEAT_INTERVAL_MS")?.toLongOrNull() ?: 5_000L


        val MISSED_THRESHOLD =
            System.getenv("HEARTBEAT_MISSED_THRESHOLD")?.toIntOrNull() ?: 3


        const val HEARTBEAT_QUESTION = "mesaj dummy"


        val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")!!
    }

    private fun now() = LocalTime.now().format(TIME_FMT)

    private suspend fun sendMessage(message: String){
        withContext(Dispatchers.IO){
            runCatching {
                messageManagerSocket.getOutputStream().write((message + "\n").toByteArray())
            }.onFailure {
                println("[${now()}] [HEARTBEAT] Eroare la trimitere: ${it.message}")
            }
        }
    }

    private fun connectToMessageManager(){
        try {
            messageManagerSocket = Socket(MESSAGE_MANAGER_HOST, MESSAGE_MANAGER_PORT)
            println("[${now()}] [HEARTBEAT] Conectat la MessageManager " +
                    "(port local: ${messageManagerSocket.localPort})")
        }catch (e: Exception){
            println("[${now()}] [HEARTBEAT] Nu ma pot conecta la MessageManager!")
            exitProcess(1)
        }
    }
    fun run() = runBlocking {

        connectToMessageManager()

        val receiveJob = launch(Dispatchers.IO) {
            val reader = BufferedReader(
                InputStreamReader(messageManagerSocket.inputStream)
            )

            var running = true
            while (running) {
                val line = reader.readLine()
                if (line == null) {
                    println("[${now()}] [HEARTBEAT] MessageManager s-a oprit!")
                    running = false
                    continue
                }

                val parts = line.split(" ", limit = 3)
                if (parts.size < 3) continue

                val (msgType, senderPort, body) = parts
                val portInt = senderPort.toIntOrNull() ?: continue

                when {
                    msgType == "raspuns_public" && body.trim() == HEARTBEAT_QUESTION -> {
                        statusMutex.withLock {
                            val status = subscriberStatuses.getOrPut(portInt) {
                                SubscriberStatus(portInt)
                            }
                            status.lastSeen = System.currentTimeMillis()
                            status.totalResponses++
                            status.missedHeartbeats = 0


                            if (!status.alive) {
                                status.alive = true
                                println("[${now()}] [HEARTBEAT] Subscriber $portInt " +
                                        "a REVENIT online!")
                            } else {
                                println("[${now()}] [HEARTBEAT] Pong de la $portInt " +
                                        "(total raspunsuri: ${status.totalResponses})")
                            }
                        }
                    }

                    // Orice alt mesaj public — inregistram subscriber-ul daca e nou
                    msgType == "intrebare_publica" || msgType == "raspuns_public" -> {
                        statusMutex.withLock {
                            if (!subscriberStatuses.containsKey(portInt)) {
                                subscriberStatuses[portInt] = SubscriberStatus(portInt)
                                println("[${now()}] [HEARTBEAT]  Subscriber nou " +
                                        "detectat: $portInt")
                            }
                        }
                    }
                }
            }
        }

        val sendJob = launch {
            var beatCount = 0
            while (true) {
                delay(HEARTBEAT_INTERVAL_MS)
                beatCount++
                println("\n[${now()}] [HEARTBEAT] -- Trimit heartbeat #$beatCount ──")


                sendMessage("intrebare_publica all $HEARTBEAT_QUESTION")
            }
        }


        val watchdogJob = launch {
            while (true) {
                // Verificam la fiecare interval + 1s (dupa ce ar fi trebuit sa vina pong)
                delay(HEARTBEAT_INTERVAL_MS + 1_000L)

                statusMutex.withLock {
                    if (subscriberStatuses.isEmpty()) {
                        println("[${now()}] [WATCHDOG] Niciun subscriber inregistrat inca.")
                        return@withLock
                    }

                    println("[${now()}] [WATCHDOG] -- Raport stare subscriberi --")

                    subscriberStatuses.forEach { (port, status) ->
                        val elapsed = System.currentTimeMillis() - status.lastSeen
                        val elapsedSec = elapsed / 1000

                        if (status.alive) {
                            if (elapsed > HEARTBEAT_INTERVAL_MS * MISSED_THRESHOLD) {
                                status.missedHeartbeats++
                                status.alive = false
                                println("[${now()}] [WATCHDOG]  Subscriber $port " +
                                        "NU raspunde de ${elapsedSec}s " +
                                        "(${status.missedHeartbeats} heartbeat-uri ratate) " +
                                        "-> CONSIDERAT OFFLINE")
                            } else {
                                println("[${now()}] [WATCHDOG]  $port " +
                                        "| ultima activitate: acum ${elapsedSec}s " +
                                        "| raspunsuri totale: ${status.totalResponses}")
                            }
                        } else {
                            println("[${now()}] [WATCHDOG]  $port " +
                                    "| OFFLINE de ${elapsedSec}s " +
                                    "| raspunsuri ratate: ${status.missedHeartbeats}")
                        }
                    }

                    val alive = subscriberStatuses.values.count { it.alive }
                    val dead  = subscriberStatuses.size - alive
                    println("[${now()}] [WATCHDOG] Sumar: $alive online, $dead offline\n")
                }
            }
        }

        val cliJob = launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(System.`in`))
            println("[${now()}] [HEARTBEAT] Comenzi disponibile: 'status', 'quit'")

            var running = true
            while (running) {
                val cmd = reader.readLine()?.trim()?.lowercase()
                if (cmd == null) { running = false; continue }
                when (cmd) {
                    "status" -> {
                        statusMutex.withLock {
                            println("\n================ STATUS COMPLET ================")
                            if (subscriberStatuses.isEmpty()) {
                                println("  Niciun subscriber cunoscut.")
                            } else {
                                subscriberStatuses.values.forEach { s ->
                                    val stare = if (s.alive) "ONLINE" else "OFFLINE"
                                    val elapsed = (System.currentTimeMillis() - s.lastSeen) / 1000
                                    println("  Port ${s.port}: $stare | " +
                                            "ultima activitate: acum ${elapsed}s | " +
                                            "raspunsuri: ${s.totalResponses} | " +
                                            "heartbeat-uri ratate: ${s.missedHeartbeats}")
                                }
                            }
                            println("═══════════════════════════════════════════════\n")
                        }
                    }
                    "quit", "exit" -> {
                        println("[${now()}] [HEARTBEAT] Oprire...")
                        receiveJob.cancel()
                        sendJob.cancel()
                        watchdogJob.cancel()
                        running = false
                    }
                    else -> println("Comanda necunoscuta: '$cmd'. Incearca 'status' sau 'quit'.")
                }
            }
        }

        receiveJob.join()

        println("[${now()}] [HEARTBEAT] Microserviciul se opreste.")
        listOf(sendJob, watchdogJob, cliJob).forEach { it.cancel() }
        runCatching { messageManagerSocket.close() }
    }
}

fun main() {
    HeartbeatMicroservice().run()
}