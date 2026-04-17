import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.system.exitProcess

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class BiddingProcessorMicroservice {
    private var biddingProcessorSocket: ServerSocket
    private lateinit var auctioneerSocket: Socket
    private var receiveProcessedBidsObservable: Observable<String>
    private val subscriptions         = CompositeDisposable()
    private val processedBidsQueue: Queue<Message> = LinkedList()


    private val journal = ExecutionJournal("bidding_processor")

    companion object Constants {
        const val BIDDING_PROCESSOR_PORT = 1700
        const val AUCTIONEER_PORT        = 1500
        const val AUCTIONEER_HOST        = "localhost"
    }

    init {
        biddingProcessorSocket = ServerSocket(BIDDING_PROCESSOR_PORT)
        println("BiddingProcessorMicroservice se executa pe portul: ${biddingProcessorSocket.localPort}")
        println("Se asteapta ofertele pentru finalizarea licitatiei...")


        val unfinished = journal.getUnfinishedOperations()
        if (unfinished.isNotEmpty()) {
            println("BiddingProcessor: detectate operații neterminate – se reia procesarea...")
            unfinished.forEach { (opId, data) ->
                when {
                    opId.startsWith("receive_processed_") -> {
                        try {
                            val msg = Message.deserialize(data.toByteArray())
                            processedBidsQueue.add(msg)
                            journal.logEnd(opId)
                            println("  ✓ Ofertă procesată recuperată: $msg")
                        } catch (e: Exception) {
                            println("  ✗ Nu s-a putut recupera oferta '$opId': ${e.message}")
                        }
                    }
                    opId.startsWith("decide_winner_") -> {
                        println("  ↻ Determinarea câștigătorului a fost întreruptă – va fi reluată.")
                    }
                }
            }
        }


        val messageProcessorConnection = biddingProcessorSocket.accept()
        println("S-a conectat MessageProcessorMicroservice!")

        val bufferReader = BufferedReader(InputStreamReader(messageProcessorConnection.inputStream))

        receiveProcessedBidsObservable = Observable.create { emitter ->
            while (true) {
                val receivedMessage = bufferReader.readLine()

                if (receivedMessage == null) {
                    bufferReader.close()
                    messageProcessorConnection.close()
                    emitter.onError(Exception("Eroare: MessageProcessorMicroservice ${messageProcessorConnection.port} a fost deconectat."))
                    break
                }

                if (Message.deserialize(receivedMessage.toByteArray()).body == "final") {
                    emitter.onComplete()

                    val senderId           = "${messageProcessorConnection.localAddress}:${messageProcessorConnection.localPort}"
                    val finishedBidsMessage = Message.create(senderId, "am primit tot")
                    messageProcessorConnection.getOutputStream().write(finishedBidsMessage.serialize())
                    messageProcessorConnection.getOutputStream().flush()
                    messageProcessorConnection.close()
                    break
                } else {
                    emitter.onNext(receivedMessage)
                }
            }
        }
    }

    private fun receiveProcessedBids() {
        val receiveProcessedBidsSubscription = receiveProcessedBidsObservable.subscribeBy(
            onNext = {
                val message = Message.deserialize(it.toByteArray())
                val opId    = "receive_processed_${message.senderIpPort}_${message.timestamp.time}"

                journal.logStart(opId, it.trim())
                println(message)
                processedBidsQueue.add(message)
                journal.logEnd(opId)
            },
            onComplete = { decideAuctionWinner() },
            onError    = { println("Eroare: $it") }
        )
        subscriptions.add(receiveProcessedBidsSubscription)
    }

    private fun decideAuctionWinner() {
        val decideOpId = "decide_winner_${System.currentTimeMillis()}"
        journal.logStart(decideOpId, "determinare_castigator")

        val winner: Message? = processedBidsQueue.toList().maxByOrNull {
            it.body.split(" ")[1].toInt()
        }

        println("Castigatorul este: ${winner?.sender} (${winner?.nume}) cu oferta: ${winner?.body}")

        try {
            auctioneerSocket = Socket(AUCTIONEER_HOST, AUCTIONEER_PORT)

            if (winner != null) {
                auctioneerSocket.getOutputStream().write(winner.serialize())
                auctioneerSocket.getOutputStream().flush()
                println("Am anuntat castigatorul catre AuctioneerMicroservice.")
            } else {
                println("Nu a existat niciun castigator (lista vida).")
                val emptyWinnerMessage = Message.create("BiddingProcessor", "licitez 0")
                auctioneerSocket.getOutputStream().write(emptyWinnerMessage.serialize())
                auctioneerSocket.getOutputStream().flush()
            }

            auctioneerSocket.close()
            journal.logEnd(decideOpId)
        } catch (e: Exception) {
            println("Nu ma pot conecta la Auctioneer! -> ${e.message}")
            biddingProcessorSocket.close()
            exitProcess(1)
        } finally {
            subscriptions.dispose()
        }
    }

    // Adaugă această funcție în clasa ta
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

        startHeartbeat("BiddingProcessorMicroservice")

        receiveProcessedBids()
    }
}

fun main(args: Array<String>) {
    val biddingProcessorMicroservice = BiddingProcessorMicroservice()
    biddingProcessorMicroservice.run()
}