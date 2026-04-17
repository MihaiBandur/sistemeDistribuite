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

class AuctioneerMicroservice {
    private var auctioneerSocket: ServerSocket
    private lateinit var messageProcessorSocket: Socket
    private var receiveBidsObservable: Observable<String>
    private val subscriptions = CompositeDisposable()
    private val bidQueue: Queue<Message> = LinkedList<Message>()
    private val bidderConnections: MutableList<Socket> = mutableListOf()

    companion object Constants {
        const val MESSAGE_PROCESSOR_HOST = "localhost"
        const val MESSAGE_PROCESSOR_PORT = 1600
        const val AUCTIONEER_PORT = 1500
        const val AUCTION_DURATION: Long = 15_000 // licitatia dureaza 15 secunde
    }

    init {
        auctioneerSocket = ServerSocket(AUCTIONEER_PORT)
        auctioneerSocket.setSoTimeout(AUCTION_DURATION.toInt())
        println("AuctioneerMicroservice se executa pe portul: ${auctioneerSocket.localPort}")
        println("Se asteapta oferte de la bidderi timp de ${AUCTION_DURATION / 1000} secunde...")

        // Se creeaza obiectul Observable cu care se genereaza evenimente cand se primesc oferte
        receiveBidsObservable = Observable.create { emitter ->
            // se asteapta conexiuni din partea bidderilor
            while (true) {
                try {
                    val bidderConnection = auctioneerSocket.accept()
                    bidderConnections.add(bidderConnection)

                    // se citeste mesajul de la bidder de pe socketul TCP
                    val bufferReader = BufferedReader(InputStreamReader(bidderConnection.inputStream))
                    val receivedMessage = bufferReader.readLine()

                    // daca se primeste un mesaj gol (NULL), cealalta parte a fost inchisa
                    if (receivedMessage == null) {
                        bufferReader.close()
                        bidderConnection.close()
                        emitter.onError(Exception("Eroare: Bidder-ul ${bidderConnection.port} a fost deconectat."))
                    } else {
                        // se emite ce s-a citit ca si element in fluxul de mesaje
                        emitter.onNext(receivedMessage)
                    }
                } catch (e: SocketTimeoutException) {
                    // daca au trecut cele 15 secunde, licitatia s-a incheiat
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
        // se incepe prin a primi ofertele de la bidderi
        val receiveBidsSubscription = receiveBidsObservable.subscribeBy(
            onNext = {
                val message = Message.deserialize(it.toByteArray())
                println("Am primit oferta: $message")
                bidQueue.add(message)
            },
            onComplete = {
                // licitatia s-a incheiat
                println("Licitatia s-a incheiat! Se trimit ofertele spre procesare...")
                forwardBids()
            },
            onError = { println("Eroare la primirea ofertelor: $it") }
        )
        subscriptions.add(receiveBidsSubscription)
    }

    private fun forwardBids() {
        try {
            messageProcessorSocket = Socket(MESSAGE_PROCESSOR_HOST, MESSAGE_PROCESSOR_PORT)

            val forwardSubscription = Observable.fromIterable(bidQueue).subscribeBy(
                onNext = {
                    // trimitere mesaje catre procesorul de mesaje
                    messageProcessorSocket.getOutputStream().write(it.serialize())
                    messageProcessorSocket.getOutputStream().flush()
                    println("Am trimis catre MessageProcessor mesajul: $it")
                },
                onComplete = {
                    println("Am trimis toate ofertele catre MessageProcessor.")

                    val senderInfo = "${messageProcessorSocket.localAddress}:${messageProcessorSocket.localPort}"
                    val bidEndMessage = Message.create(senderInfo, "final")

                    messageProcessorSocket.getOutputStream().write(bidEndMessage.serialize())
                    messageProcessorSocket.getOutputStream().flush()

                    // dupa ce s-a terminat licitatia, se asteapta confirmarea
                    val bufferReader = BufferedReader(InputStreamReader(messageProcessorSocket.inputStream))
                    bufferReader.readLine() // Asteptam confirmarea

                    messageProcessorSocket.close()
                    finishAuction()
                },
                onError = {
                    println("Eroare la trimiterea ofertelor mai departe: $it")
                }
            )
            subscriptions.add(forwardSubscription)
        } catch (e: Exception) {
            println("Nu ma pot conecta la MessageProcessor pe portul $MESSAGE_PROCESSOR_PORT!")
            auctioneerSocket.close()
            exitProcess(1)
        }
    }

    private fun finishAuction() {
        println("Astept rezultatul de la BiddingProcessor...")
        // se asteapta rezultatul licitatiei de la BiddingProcessor
        try {
            val biddingProcessorConnection = auctioneerSocket.accept()
            val bufferReader = BufferedReader(InputStreamReader(biddingProcessorConnection.inputStream))

            // se citeste rezultatul licitatiei de pe socketul TCP
            val receivedMessage = bufferReader.readLine()
            val result: Message = Message.deserialize(receivedMessage.toByteArray())

            val winningPrice = result.body.split(" ")[1].toInt()
            println("Am primit rezultatul licitatiei de la BiddingProcessor: ${result.sender} a castigat cu pretul: $winningPrice")

            // se creeaza mesajele pentru rezultatele licitatiei
            val winningMessage = Message.create(
                auctioneerSocket.localSocketAddress.toString(),
                "Licitatie castigata! Pret castigator: $winningPrice"
            )
            val losingMessage = Message.create(
                auctioneerSocket.localSocketAddress.toString(),
                "Licitatie pierduta..."
            )

            // se anunta castigatorul si invinsii
            bidderConnections.forEach {
                try {
                    if (it.remoteSocketAddress.toString() == result.sender) {
                        it.getOutputStream().write(winningMessage.serialize())
                    } else {
                        it.getOutputStream().write(losingMessage.serialize())
                    }
                    it.getOutputStream().flush()
                } catch (e: Exception) {
                    println("Eroare la anuntarea bidder-ului de pe portul ${it.port}")
                } finally {
                    it.close()
                }
            }

            biddingProcessorConnection.close()
        } catch (e: Exception) {
            println("Eroare la preluarea rezultatului de la BiddingProcessor: ${e.message}")
            auctioneerSocket.close()
            exitProcess(1)
        } finally {
            // se elibereaza memoria din multimea de Subscriptions
            subscriptions.dispose()
            println("Auctioneer-ul și-a încheiat execuția cu succes.")
        }
    }

    fun run() {
        receiveBids()
    }
}

fun main(args: Array<String>) {
    val auctioneerMicroservice = AuctioneerMicroservice()
    auctioneerMicroservice.run()
}