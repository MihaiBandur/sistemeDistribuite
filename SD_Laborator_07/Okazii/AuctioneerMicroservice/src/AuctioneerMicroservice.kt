import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import kotlin.system.exitProcess

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class AuctioneerMicroservice {
    private var auctioneerSocket: ServerSocket
    private lateinit var messageProcessorSocket: Socket
    private var receiveBidsObservable: Observable<String>
    private val subscriptions   = CompositeDisposable()
    private val bidQueue: Queue<Message>       = LinkedList()
    private val bidderConnections: MutableList<Socket> = mutableListOf()

    // ── Jurnal de execuție ────────────────────────────────────────────────────
    private val journal = ExecutionJournal("auctioneer")

    companion object Constants {
        const val MESSAGE_PROCESSOR_HOST = "localhost"
        const val MESSAGE_PROCESSOR_PORT = 1600
        const val AUCTIONEER_PORT        = 1500
        const val AUCTION_DURATION: Long = 15_000 // licitația durează 15 secunde
    }

    init {
        auctioneerSocket = ServerSocket(AUCTIONEER_PORT)
        auctioneerSocket.setSoTimeout(AUCTION_DURATION.toInt())
        println("AuctioneerMicroservice se executa pe portul: ${auctioneerSocket.localPort}")
        println("Se asteapta oferte de la bidderi timp de ${AUCTION_DURATION / 1000} secunde...")

        // ── Recovery ─────────────────────────────────────────────────────────
        val unfinished = journal.getUnfinishedOperations()
        if (unfinished.isNotEmpty()) {
            println("Auctioneer: detectate operatii neterminate – se reia procesarea...")
            unfinished.forEach { (opId, data) ->
                println("  • Reia operatia '$opId'")
                when {
                    opId.startsWith("receive_bid_") -> {
                        // Ofertă primită dar nepusă în coadă – reconstituim din date
                        try {
                            val msg = Message.deserialize(data.toByteArray())
                            bidQueue.add(msg)
                            journal.logEnd(opId)
                            println("  ✓ Oferta recuperată: $msg")
                        } catch (e: Exception) {
                            println("  ✗ Nu s-a putut recupera oferta: ${e.message}")
                        }
                    }
                    opId.startsWith("forward_bids_") -> {
                        // Redirecționarea a fost întreruptă; se va relua în forwardBids()
                        println("  ↻ Redirecționare în curs – va fi reluată după colectare.")
                    }
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        receiveBidsObservable = Observable.create { emitter ->
            while (true) {
                try {
                    val bidderConnection = auctioneerSocket.accept()
                    bidderConnections.add(bidderConnection)

                    val bufferReader   = BufferedReader(InputStreamReader(bidderConnection.inputStream))
                    val receivedMessage = bufferReader.readLine()

                    if (receivedMessage == null) {
                        bufferReader.close()
                        bidderConnection.close()
                        emitter.onError(Exception("Eroare: Bidder-ul ${bidderConnection.port} a fost deconectat."))
                    } else {
                        emitter.onNext(receivedMessage)
                    }
                } catch (e: SocketTimeoutException) {
                    emitter.onComplete()
                    break
                } catch (e: Exception) {
                    emitter.onError(e)
                    break
                }
            }
        }
    }

    private fun receiveBids() {
        val receiveBidsSubscription = receiveBidsObservable.subscribeBy(
            onNext = {
                val message = Message.deserialize(it.toByteArray())
                val opId    = "receive_bid_${message.senderIpPort}_${message.timestamp.time}"

                journal.logStart(opId, it.trim())        // salvăm oferta brută
                println("Am primit oferta: $message")
                bidQueue.add(message)
                journal.logEnd(opId)
            },
            onComplete = {
                println("Licitatia s-a incheiat! Se trimit ofertele spre procesare...")
                forwardBids()
            },
            onError = { println("Eroare la primirea ofertelor: $it") }
        )
        subscriptions.add(receiveBidsSubscription)
    }

    private fun forwardBids() {
        val forwardOpId = "forward_bids_${System.currentTimeMillis()}"
        journal.logStart(forwardOpId, "trimitere_catre_message_processor")

        try {
            messageProcessorSocket = Socket(MESSAGE_PROCESSOR_HOST, MESSAGE_PROCESSOR_PORT)

            val forwardSubscription = Observable.fromIterable(bidQueue).subscribeBy(
                onNext = {
                    val itemOpId = "forward_item_${it.senderIpPort}_${it.timestamp.time}"
                    journal.logStart(itemOpId, String(it.serialize()).trim())

                    messageProcessorSocket.getOutputStream().write(it.serialize())
                    messageProcessorSocket.getOutputStream().flush()
                    println("Am trimis catre MessageProcessor mesajul: $it")

                    journal.logEnd(itemOpId)
                },
                onComplete = {
                    println("Am trimis toate ofertele catre MessageProcessor.")

                    val senderInfo    = "${messageProcessorSocket.localAddress}:${messageProcessorSocket.localPort}"
                    val bidEndMessage = Message.create(senderInfo, "final")

                    messageProcessorSocket.getOutputStream().write(bidEndMessage.serialize())
                    messageProcessorSocket.getOutputStream().flush()

                    val bufferReader = BufferedReader(InputStreamReader(messageProcessorSocket.inputStream))
                    bufferReader.readLine() // confirmarea de la MessageProcessor

                    messageProcessorSocket.close()
                    journal.logEnd(forwardOpId)
                    finishAuction()
                },
                onError = { println("Eroare la trimiterea ofertelor mai departe: $it") }
            )
            subscriptions.add(forwardSubscription)
        } catch (e: Exception) {
            println("Nu ma pot conecta la MessageProcessor pe portul $MESSAGE_PROCESSOR_PORT!")
            auctioneerSocket.close()
            exitProcess(1)
        }
    }

    private fun finishAuction() {
        val finishOpId = "finish_auction_${System.currentTimeMillis()}"
        journal.logStart(finishOpId, "asteptare_rezultat_bidding_processor")
        println("Astept rezultatul de la BiddingProcessor...")

        try {
            val biddingProcessorConnection = auctioneerSocket.accept()
            val bufferReader               = BufferedReader(InputStreamReader(biddingProcessorConnection.inputStream))

            val receivedMessage = bufferReader.readLine()
            val result: Message = Message.deserialize(receivedMessage.toByteArray())

            val winningPrice = result.body.split(" ")[1].toInt()
            println("Castigator: ${result.sender} (${result.nume}) cu pretul: $winningPrice")

            val winningMessage = Message.create(
                auctioneerSocket.localSocketAddress.toString(),
                "Licitatie castigata! Pret castigator: $winningPrice"
            )
            val losingMessage = Message.create(
                auctioneerSocket.localSocketAddress.toString(),
                "Licitatie pierduta..."
            )

            bidderConnections.forEach {
                val notifyOpId = "notify_bidder_${it.port}"
                journal.logStart(notifyOpId, it.remoteSocketAddress.toString())
                try {
                    val msg = if (it.remoteSocketAddress.toString() == result.sender) winningMessage else losingMessage
                    it.getOutputStream().write(msg.serialize())
                    it.getOutputStream().flush()
                    journal.logEnd(notifyOpId)
                } catch (e: Exception) {
                    println("Eroare la anuntarea bidder-ului de pe portul ${it.port}")
                } finally {
                    it.close()
                }
            }

            biddingProcessorConnection.close()
            journal.logEnd(finishOpId)
        } catch (e: Exception) {
            println("Eroare la preluarea rezultatului de la BiddingProcessor: ${e.message}")
            auctioneerSocket.close()
            exitProcess(1)
        } finally {
            subscriptions.dispose()
            println("Auctioneer-ul și-a încheiat execuția cu succes.")
        }
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

        startHeartbeat("AuctioneerMicroservice")
        receiveBids()
    }
}

fun main(args: Array<String>) {
    val auctioneerMicroservice = AuctioneerMicroservice()
    auctioneerMicroservice.run()
}
