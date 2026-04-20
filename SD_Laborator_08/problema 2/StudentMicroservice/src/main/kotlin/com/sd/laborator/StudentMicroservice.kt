package com.sd.laborator

import com.sun.swing.internal.plaf.basic.resources.basic_es
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Socket
import kotlin.system.exitProcess


class StudentMicroservice {
    private lateinit var questionDatabase: MutableList<Pair<String, String>>
    private lateinit var messageManagerSocket: Socket

    companion object{
        val MESSAGE_MANAGER_HOST = System.getenv("MESSAGE_MANAGER_HOST") ?: "localhost"
        const val MESSAGE_MANAGER_PORT = 1500
    }

    init {
        val lines = File("questions_database.txt").readLines()
        questionDatabase = mutableListOf()
        for (i in 0..(lines.size - 1) step 2){
            questionDatabase.add(Pair(lines[i], lines[i+1]))
        }

    }

    private fun subscribeToMessageManager(){
        try {
            messageManagerSocket = Socket(MESSAGE_MANAGER_HOST, MESSAGE_MANAGER_PORT)
            println("M-am conectat la MessageManager!")
        }catch (e: Exception){
            println("Nu ma pot conecta la MessageManager!")
            exitProcess(1)
        }
    }

    private  fun respondToQuestion(question: String): String =
        questionDatabase.firstOrNull {
            it.first == question
        }?.second ?: "Nu a fost gasit niciun raspuns"

    private suspend fun sendMessage(message: String){
        withContext(Dispatchers.IO){
            messageManagerSocket.getOutputStream().write((message + "\n").toByteArray())
        }
    }

    fun run() = runBlocking {
        subscribeToMessageManager()
        println("StudentMicroservice se executa pe portul: ${messageManagerSocket.localPort}")
        println("Se asteapta mesaje...")
        println("Poti pune intrebari scriind in terminal:")
        println(" -> public <mesaj>   (Ex: public Care e tema?)")
        println(" -> <port> <mesaj>   (Ex: 52000 Mai este mult?)")

        val inputJob = launch(Dispatchers.IO){
            val reader = BufferedReader(InputStreamReader(System.`in`))
            while (true){
                val line = reader.readLine() ?: break
                when {
                    line.startsWith("public ") ->{
                        val question = line.removePrefix("public ")
                        sendMessage("intrebare_publica all $question")
                    }
                    else ->{
                        val parts = line.split(" ", limit = 2)
                        if (parts.size == 2 && parts[0].toIntOrNull() != null) {
                            sendMessage("intrebare_privata ${parts[0]} ${parts[1]}")
                        } else {
                            println("Format gresit! Foloseste 'public <mesaj>' sau '<port> <mesaj>'")
                        }
                    }
                }
            }
        }
        val receiveJob = launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(messageManagerSocket.inputStream))

            while (true){
                val response = reader.readLine()

                if(response == null) {
                    println("MessageManager s-a oprit.")
                    break
                }
                    launch {
                        val parts = response!!.split(" ", limit = 3)
                        if(parts.size < 3) return@launch

                        val (messageType, senderPort, messageBody) = parts

                        when(messageType){
                            "intrebare_publica", "intrebare_privata" ->{
                                val tip = if (messageType == "intrebare_publica") "PUBLIC" else "PRIVAT"
                                println("\n[INTREBARE $tip] de la $senderPort: \"$messageBody\"")

                                val raspuns = respondToQuestion(messageBody)
                                val replyType = if (messageType == "intrebare_publica")
                                    "raspuns_public" else "raspuns_privat"
                                val dest = if (messageType == "intrebare_publica") "all" else senderPort

                                println(" -> Trimit $replyType: \"$raspuns\"")
                                sendMessage("$replyType $dest $raspuns")
                            }
                            "raspuns_public", "raspuns_privat" ->{
                                val tip = if (messageType == "raspuns_public") "PUBLIC" else "PRIVAT"
                                println("\n[RASPUNS $tip] de la $senderPort: \"$messageBody\"")
                            }
                        }
                    }
                }
            }
        select<Unit> {
            inputJob.onJoin{receiveJob.cancel()}
            receiveJob.onJoin{inputJob.cancel()}
        }
        println("StudentMicroservice se opreste.")
        runCatching { messageManagerSocket.close() }
    }
}

fun main(){
    StudentMicroservice().run()
}