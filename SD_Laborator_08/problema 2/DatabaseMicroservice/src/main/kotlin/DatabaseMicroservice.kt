package com.sd.laborator

import com.sun.security.ntlm.Client
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager

class DatabaseMicroservice {
    companion object{
        const val DB_PORT = 1700

        const val DB_URL = "jdbc:sqlite:/db/note_studenti.db"
    }

    private fun initDatabase(){
        DriverManager.getConnection(DB_URL).use { conn->
            val stmt = conn.createStatement()
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS note (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_port INTEGER,
                    valoare REAL
                )
                """.trimIndent()
            )
        }
    }

    private fun adaugaNota(portStudent: Int, nota: Double): String{
        return try {
            DriverManager.getConnection(DB_URL).use { conn ->
                val pstmt = conn.prepareStatement("INSERT INTO note (student_port, valoare) VALUES (?, ?)")
                pstmt.setInt(1, portStudent)
                pstmt.setDouble(2, nota)
                pstmt.executeUpdate()
            }
            "Nota $nota a fost salvata cu succes pentru studentul $portStudent."
        }catch (e: Exception){
            "Eroare la salvarea notei: ${e.message}"
        }
    }

    private fun calculeazaMedia(portStudent: Int): String{
        return try {
            DriverManager.getConnection(DB_URL).use { connection ->
                val pstmt = connection.prepareStatement("SELECT AVG(valoare) as medie, COUNT(valoare) as numar_note FROM note WHERE student_port = ?")
                pstmt.setInt(1, portStudent)
                val rs = pstmt.executeQuery()
                if(rs.next() && rs.getInt("numar_note") > 0){
                    val medie = rs.getDouble("medie")
                    "Eveniment FINISH: Media studentului $portStudent este $medie (din ${rs.getInt("numar_note")} note)."
                }else{
                    "Studentul $portStudent nu are nicio nota inregistrata."
                }
            }
        }catch (e : Exception){
            "Eroare la calculul mediei: ${e.message}"
        }
    }

    private suspend fun handleClient(client: Socket){
        withContext(Dispatchers.IO){
            val reader = BufferedReader(InputStreamReader(client.inputStream))
            try {
                while (true){
                    val request = reader.readLine() ?: break
                    println("Am primit cererea: $request")
                    val parts = request.split(" ")
                    val response = when(parts[0]){
                        "NOTA" -> {
                            if (parts.size == 3){
                                adaugaNota(parts[1].toInt(), parts[2].toDouble())
                            }else "Format gresit. Folositi: NOTA <port> <valoare>"
                        }
                        "FINISH" ->{
                            if (parts.size == 2) {
                                calculeazaMedia(parts[1].toInt())
                            } else "Format gresit. Folositi: FINISH <port>"
                        }
                        else -> "Comanda de baza de date necunoscuta."
                    }
                    client.getOutputStream().write((response + "\n").toByteArray())
                }
            }catch (e: Exception){
                println("Eroare DB client: ${e.message}")
            }finally {
                runCatching { client.close() }
            }
        }
    }

    fun run() = runBlocking {
        println("Initializare baza de date SQLite...")
        initDatabase()

        val serverSocket = ServerSocket(DB_PORT)
        println("DatabaseMicroservice asculta pe portul: ${serverSocket.localPort}")

        withContext(Dispatchers.IO) {
            while (true) {
                val client = serverSocket.accept()
                launch {
                    handleClient(client)
                }
            }
        }
    }
}

fun main(){
    DatabaseMicroservice().run()
}