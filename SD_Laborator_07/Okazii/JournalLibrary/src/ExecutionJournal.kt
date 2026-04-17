import com.sun.xml.internal.ws.api.message.Message
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ExecutionJournal(private val microserviceName: String) {

    private val logFile = File("${microserviceName}_journal.txt")
    private val masterPort = 3000
    private val udpSocket = DatagramSocket()
    private val masterAddress = InetAddress.getByName("localhost")

    private fun sendToMaster(message: String){
        try {
            val logMessage = "$microserviceName | $message".toByteArray()
            val packet = DatagramPacket(logMessage, logMessage.size, masterAddress, masterPort)
            udpSocket.send(packet)
        }catch (e: Exception){

        }
    }

    fun logStart(operationId: String, data: String) {
        val entry = "START|$operationId|$data"
        logFile.appendText("$entry\n")
        sendToMaster(entry)
    }

    fun logEnd(operationId: String) {
        val entry = "END|$operationId|"
        logFile.appendText("$entry\n")
        sendToMaster(entry)
    }

    fun logMetric(metricType: String, value: String) {
        val entry = "METRIC|$metricType|$value"
        logFile.appendText("$entry\n")
        sendToMaster(entry)
    }

    fun getUnfinishedOperations(): Map<String, String> {
        if (!logFile.exists()) return emptyMap()

        val pending = mutableMapOf<String, String>()

        logFile.readLines().forEach { line ->
            val parts = line.split("|", limit = 3)
            if (parts.size == 3) {
                val status = parts[0]
                val opId = parts[1]
                val data = parts[2]

                if (status == "START") {
                    pending[opId] = data
                } else if (status == "END") {
                    pending.remove(opId)
                }
            }
        }
        return pending
    }
    fun clear() {
        logFile.delete()
    }
}
fun main(args: Array<String>) {
    val test_journal = ExecutionJournal("text")
    test_journal.logStart("1", "test")
    test_journal.logEnd("1")
    test_journal.clear()

}