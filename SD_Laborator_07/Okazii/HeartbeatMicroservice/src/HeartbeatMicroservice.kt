import java.net.DatagramPacket
import java.net.DatagramSocket
import kotlin.concurrent.thread
import java.io.File

class HeartbeatMicroservice {

    private val servicePaths = mapOf(
        "MessageProcessorMicroservice" to "../out/artifacts/MessageProcessorMicroservice_jar",
        "AuctioneerMicroservice" to "../out/artifacts/AuctioneerMicroservice_jar",
        "BiddingProcessorMicroservice" to "../out/artifacts/BiddingProcessorMicroservice_jar",
        "BidderMicroservice" to "../out/artifacts/BidderMicroservice_jar"
    )
    private val registry = mutableMapOf<String, Long>()
    private val port = 2000
    private val timeoutMillis = 10_000L

    fun start(){
        println("A pornit serviciul de heartbeat")

        thread {
            val socket =DatagramSocket(port)
            val buffer = ByteArray(256)

            while (true){
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val message = String(packet.data, 0, packet.length).trim()
                if(message.startsWith("HEARTBEAT|")){
                    val serviceName = message.split("|")[1]
                    registry[serviceName] = System.currentTimeMillis()
                }
            }
        }

        thread {
            while (true){
                Thread.sleep(5000)
                val now = System.currentTimeMillis()

                val deadServices  = registry.filter {now - it.value > timeoutMillis  }.keys


                for(service in deadServices){
                    println("Alarma ~> Microserviciul $service  apicat. Se incearca repornisrea acestuia")

                    //restartService()

                    registry[service] = System.currentTimeMillis()
                }
            }
        }
    }

    private fun restartService(serviceName: String){
        val relativePath = servicePaths[serviceName]

        if (relativePath == null) {
            println("Eroare: Nu știu în ce folder se află $serviceName!")
            return
        }
        try {

            val jarFolder = File(relativePath)

            if (!File(jarFolder, "$serviceName.jar").exists()) {
                println("Eroare: Nu am găsit fișierul $serviceName.jar în folderul $relativePath!")
                return
            }

            println("Execut: java -jar $serviceName.jar din folder-ul $relativePath")

            val processBuilder = ProcessBuilder("java", "-jar", "$serviceName.jar")

            processBuilder.directory(jarFolder)

            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

            processBuilder.start()

            println("Servicul $serviceName a fost repornit cu succes")
        }catch (e: Exception){
            println("Nu s-a putut reporni micrservociul $serviceName din folder-ul $relativePath")
        }
    }

}

fun main(){
    HeartbeatMicroservice().start()
}