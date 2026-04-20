package com.sd.laborator

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

class TeacherMicroservice {
    private lateinit var messageManagerSocket: Socket
    private lateinit var teacherServerSocket: ServerSocket

    private val guiMutex = Mutex()
    private var guiSocket: Socket? = null

    companion object {
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

    private suspend fun sendToGui(message: String){
        guiMutex.withLock {
            guiSocket?.let { socket ->
                if(!socket.isClosed){
                    withContext(Dispatchers.IO){
                        runCatching {
                            socket.getOutputStream().write((message + "\n").toByteArray())
                        }.onFailure {
                            println("Eroare trimitere GUI: ${it.message}")
                            guiSocket = null
                        }
                    }
                }
            }
        }
    }

    fun run() = runBlocking {
        subscribeToMessageManager()
        teacherServerSocket = ServerSocket(TEACHER_PORT)
        println("TeacherMicroservice se executa pe portul: ${teacherServerSocket.localPort}")

        launch(Dispatchers.IO) {
            val mmReader = BufferedReader(InputStreamReader(messageManagerSocket.inputStream))
            while (true){
                val incoming = mmReader.readLine() ?: break
                val parts = incoming.split(" ", limit = 3)

                if(parts.size >= 3){
                    val(messageType, sender, body) = parts
                    println("[$messageType] de la $sender: $body")

                    if(messageType.startsWith("raspuns")){
                        sendToGui("[$sender]: $body")
                    }
                }
            }
            println("MessageManager s-a oprit.")
        }

        withContext(Dispatchers.IO){
            while (true){
                val clientConnection = teacherServerSocket.accept()
                println("GUI conectat: ${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")

                guiMutex.withLock {
                    guiSocket?.runCatching { close() }
                    guiSocket = clientConnection
                }
                launch {
                    handleGuiConnection(clientConnection)
                }
            }
        }

    }
    private suspend fun handleGuiConnection(clientConnection: Socket) {
        withContext(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(clientConnection.inputStream))

            try {
                while (true) {
                    val question = reader.readLine() ?: break
                    println("Intrebare de la GUI: $question")
                    messageManagerSocket.getOutputStream()
                        .write(("intrebare_publica all $question\n").toByteArray())
                }
            } catch (e: Exception) {
                println("Eroare conexiune GUI: ${e.message}")
            } finally {
                println("GUI deconectat.")
                guiMutex.withLock {
                    if (guiSocket == clientConnection) guiSocket = null
                }
                runCatching { clientConnection.close() }
            }
        }
    }
}
fun main(){
    TeacherMicroservice().run()
}