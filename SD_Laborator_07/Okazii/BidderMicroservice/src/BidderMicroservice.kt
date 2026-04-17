import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import kotlin.random.Random
import kotlin.system.exitProcess

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class BidderMicroservice(
    private val nume: String,
    private val telefon: String,
    private val email: String
) {
    private var auctioneerSocket: Socket
    private var auctionResultObservable: Observable<String>
    private var myIdentity: String = "[BIDDER_NECONECTAT]"

    private var journal: ExecutionJournal

    companion object Constants {
        const val AUCTIONEER_HOST = "localhost"
        const val AUCTIONEER_PORT = 1500
        const val MAX_BID = 10_000
        const val MIN_BID = 1_000
    }

    init {
        try {
            auctioneerSocket = Socket(AUCTIONEER_HOST, AUCTIONEER_PORT)
            myIdentity = "[$nume / ${auctioneerSocket.localPort}]"
            journal     = ExecutionJournal("bidder_${auctioneerSocket.localPort}")
            println("$myIdentity M-am conectat la Auctioneer!")


            val unfinished = journal.getUnfinishedOperations()
            if (unfinished.isNotEmpty()) {
                println("$myIdentity Detectate operatii neterminate – se reia procesarea...")
                unfinished.forEach { (opId, data) ->
                    println("$myIdentity  Reia operatia '$opId': $data")
                    try {
                        val msg = Message.deserialize(data.toByteArray())
                        auctioneerSocket.getOutputStream().write(msg.serialize())
                        auctioneerSocket.getOutputStream().flush()
                        journal.logEnd(opId)
                        println("$myIdentity  Oferta retrimisa cu succes.")
                    } catch (e: Exception) {
                        println("$myIdentity Nu s-a putut retrimite oferta: ${e.message}")
                    }
                }
            }
            auctionResultObservable = Observable.create { emitter ->
                try {
                    val bufferReader = BufferedReader(InputStreamReader(auctioneerSocket.inputStream))
                    val receivedMessage = bufferReader.readLine()
                    if (receivedMessage == null) {
                        emitter.onError(Exception("AuctioneerMicroservice s-a deconectat."))
                    } else {
                        emitter.onNext(receivedMessage)
                        emitter.onComplete()
                    }
                    bufferReader.close()
                    auctioneerSocket.close()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        } catch (e: Exception) {
            println("$myIdentity Nu ma pot conecta la Auctioneer!")
            exitProcess(1)
            throw RuntimeException(e)
        }
    }

    private fun bid() {
        val pret     = Random.nextInt(MIN_BID, MAX_BID)
        val senderId = "${auctioneerSocket.localAddress}:${auctioneerSocket.localPort}"

        val biddingMessage   = Message.create(senderId, nume, telefon, email, "licitez $pret")
        val serializedMessage = biddingMessage.serialize()
        val opId              = "bid_${auctioneerSocket.localPort}_${System.currentTimeMillis()}"


        journal.logStart(opId, String(serializedMessage).trim())

        auctioneerSocket.getOutputStream().write(serializedMessage)
        auctioneerSocket.getOutputStream().flush()
        println("$myIdentity Am trimis oferta: $pret")

        journal.logEnd(opId)

    }

    private fun waitForResult() {
        println("$myIdentity Astept rezultatul licitatiei...")

        val opId = "wait_result_${auctioneerSocket.localPort}"
        journal.logStart(opId, "asteptare_rezultat")

        val auctionResultSubscription = auctionResultObservable.subscribeBy(
            onNext = {
                val resultMessage: Message = Message.deserialize(it.toByteArray())
                println("$myIdentity Rezultat licitatie: ${resultMessage.body}")
                journal.logEnd(opId)
            },
            onError = {
                println("$myIdentity Eroare la asteptarea rezultatului: ${it.message}")
            }
        )
        auctionResultSubscription.dispose()
    }

    private fun startHeartbeat(numeIdentificare: String) {
        thread(isDaemon = true) {
            val socket = DatagramSocket()
            val address = InetAddress.getByName("localhost")
            val message = "HEARTBEAT|$numeIdentificare".toByteArray()

            println("Pulsează heartbeat pentru $numeIdentificare...")

            while (true) {
                try {
                    val packet = DatagramPacket(message, message.size, address, 2000)
                    socket.send(packet)
                    Thread.sleep(3000) // Trimite un puls la fiecare 3 secunde
                } catch (e: Exception) {
                    // Erorile de rețea temporare la heartbeat sunt ignorate
                }
            }
        }
    }
    fun run() {
        startHeartbeat("BidderMicroservice")
        bid()
        waitForResult()
    }
}

fun main(args: Array<String>) {
    val bidderMicroservice = BidderMicroservice(
        nume    = "Ion Popescu",
        telefon = "0700123456",
        email   = "ion.popescu@example.com"
    )
    bidderMicroservice.run()
}