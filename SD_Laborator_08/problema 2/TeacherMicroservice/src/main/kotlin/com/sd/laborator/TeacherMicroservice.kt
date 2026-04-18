package com.sd.laborator

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class TeacherMicroservice {
    private lateinit var messageManagerSocket: Socket
    private lateinit var teacherMicroserviceServerSocket: ServerSocket


    @Volatile
    private var guiSocket: Socket? = null

    companion object Constants {
        val MESSAGE_MANAGER_HOST = System.getenv("MESSAGE_MANAGER_HOST") ?: "localhost"
        const val MESSAGE_MANAGER_PORT = 1500
        const val TEACHER_PORT = 1600
    }

    private fun subscribeToMessageManager() {
        try {
            messageManagerSocket = Socket(MESSAGE_MANAGER_HOST, MESSAGE_MANAGER_PORT)
            println("M-am conectat la MessageManager!")
        } catch (e: Exception) {
            println("Nu ma pot conecta la MessageManager!")
            exitProcess(1)
        }
    }

    public fun run() {
        subscribeToMessageManager()
        teacherMicroserviceServerSocket = ServerSocket(TEACHER_PORT)
        println("TeacherMicroservice se executa pe portul: ${teacherMicroserviceServerSocket.localPort}")

        // Thread care citeste raspunsurile de la MessageManager si le trimite la GUI
        thread {
            val mmReader = BufferedReader(InputStreamReader(messageManagerSocket.inputStream))
            while (true) {
                val incoming = mmReader.readLine() ?: break
                val parts = incoming.split(" ", limit = 3)

                if (parts.size >= 3) {
                    val (messageType, sender, body) = parts
                    println("[$messageType] de la $sender: $body")

                    if (messageType.startsWith("raspuns")) {
                        val sock = guiSocket
                        if (sock != null && !sock.isClosed) {
                            try {
                                sock.getOutputStream().write(("[$sender]: $body\n").toByteArray())
                            } catch (e: Exception) {
                                println("Eroare la trimitere catre GUI: ${e.message}")
                                guiSocket = null
                            }
                        }
                    }
                }
            }
        }

        while (true) {
            val clientConnection = teacherMicroserviceServerSocket.accept()
            println("GUI conectat: ${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")

            guiSocket = clientConnection

            thread {
                val clientBuffer = BufferedReader(InputStreamReader(clientConnection.inputStream))


                while (true) {
                    val receivedQuestion = clientBuffer.readLine() ?: break
                    println("Intrebare de la GUI: $receivedQuestion")
                    messageManagerSocket.getOutputStream()
                        .write(("intrebare_publica all $receivedQuestion\n").toByteArray())
                }

                println("GUI deconectat.")
                guiSocket = null
                clientConnection.close()
            }
        }
    }
}

fun main() {
    val teacherMicroservice = TeacherMicroservice()
    teacherMicroservice.run()
}