package com.sd.laborator



import java.net.ServerSocket

fun main(Args: Array<String>){
    val server = ServerSocket(2000)
    println("Microserviciul se executa pe portul: ${server.localPort}")
    println("Se asteapta conexiuni...")

    while (true){
        val client = server.accept()
        println("Client conectat: ${client.inetAddress.hostAddress}:${client.port}")

        client.getOutputStream().write("Hello from a dockerized microservice!\n".toByteArray())

        client.close()
    }
}