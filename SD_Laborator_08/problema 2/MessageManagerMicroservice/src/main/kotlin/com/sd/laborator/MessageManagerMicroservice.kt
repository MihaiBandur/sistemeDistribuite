package com.sd.laborator

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket


class MessageManagerMicroservice{
    private val subscribersMutex = Mutex()
    private  val subscribers: HashMap<Int, Socket> = hashMapOf()

    companion object{
        const val MESSAGE_MANAGER_PORT = 1500
    }

    private suspend fun broadcastMessage(message: String, except: Int){
        subscribersMutex.withLock {
            subscribers.forEach { (port, socket) ->
                if(port != except){
                    withContext(Dispatchers.IO){
                        runCatching {
                            socket.getOutputStream().write((message + "\n").toByteArray())

                        }
                    }
                }
            }
        }
    }

    private suspend fun respondTo(destination: Int, message: String){
        subscribersMutex.withLock {
            subscribers[destination]?.let {
                socket ->
                withContext(Dispatchers.IO){
                    runCatching {
                        socket.getOutputStream().write((message + "\n").toByteArray())
                    }
                }
            }
        }
    }

    fun run() = runBlocking {
        val serverSocket = ServerSocket(MESSAGE_MANAGER_PORT)
        println("MessageManagerMicroservice se executa pe portul: ${serverSocket.localPort}")
        println("Se asteapta conexiuni si mesaje...")

        withContext(Dispatchers.IO){
            while (true){
                val clientConnection = serverSocket.accept()

                launch {
                    handleSubscriber(clientConnection)
                }
            }
        }
    }
    private suspend fun handleSubscriber(clientConnection: Socket){
        val port = clientConnection.port
        println("Subscriber conectat: ${clientConnection.inetAddress.hostAddress}:$port")

        subscribersMutex.withLock {
            subscribers[port] = clientConnection
        }

        withContext(Dispatchers.IO){
            val reader = BufferedReader(InputStreamReader(clientConnection.inputStream))

            try {
                while (true){
                    val receivedMessage = reader.readLine()

                    if (receivedMessage == null){
                        println("Subscriber-ul $port a fost deconectat.")
                        subscribersMutex.withLock {
                            subscribers.remove(port)
                        }
                        break
                    }
                    println("Primit mesaj: $receivedMessage")
                    val parts = receivedMessage.split(" ", limit = 3)
                    if (parts.size < 3) continue

                    val (messageType, messageDestination, messageBody) = parts

                    when(messageType){
                        "intrebare_publica", "raspuns_public" ->{
                            broadcastMessage(
                                "$messageType $port $messageBody",
                                except = port )
                        }
                        "intrebare_privata", "raspuns_privat" ->{
                            respondTo(
                                messageDestination.toInt(),
                                "$messageType $port $messageBody"

                            )
                        }
                       else -> println("Tip de mesaj necunoscut: $messageType")

                    }

                }
            }catch (e: Exception){
                println("Eroare subscriber $port: ${e.message}")
                subscribersMutex.withLock { subscribers.remove(port) }
            }finally {
                runCatching { clientConnection.close() }
            }
        }
    }
}

fun  main(Args: Array<String>){
    MessageManagerMicroservice().run()
}