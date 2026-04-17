import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MasterLoggerMicroservice {
    private val masterLogFile = File("MASTER_LOG_CENTRAL.txt")
    private val port = 3000

    fun start(){
        println("Master Logger a pornit si asculta pe portul $port...")
        thread {
            val socket = DatagramSocket(port)
            val buffer = ByteArray(1024)

            while (true){
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val message = String(packet.data, 0, packet.length)
                val timestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())

                val logEntry = "[$timestamp] $message\n"
                masterLogFile.appendText(logEntry)

                println("LOG: $message")
            }
        }

    }
}

fun main(){
    MasterLoggerMicroservice().start()
}
