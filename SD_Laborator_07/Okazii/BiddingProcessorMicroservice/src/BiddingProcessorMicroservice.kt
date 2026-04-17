import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.system.exitProcess

class BiddingProcessorMicroservice {
    private var biddingProcessorSocket: ServerSocket
    private lateinit var auctioneerSocket: Socket
    private var receiveProcessedBidsObservable: Observable<String>
    private val subscriptions = CompositeDisposable()
    private val processedBidsQueue: Queue<Message> = LinkedList<Message>()

    companion object Constants {
        const val BIDDING_PROCESSOR_PORT = 1700
        const val AUCTIONEER_PORT = 1500
        const val AUCTIONEER_HOST = "localhost"
    }

    init {
        biddingProcessorSocket = ServerSocket(BIDDING_PROCESSOR_PORT)
        println("BiddingProcessorMicroservice se executa pe portul: ${biddingProcessorSocket.localPort}")
        println("Se asteapta ofertele pentru finalizarea licitatiei...")

        // se asteapta mesaje primite de la MessageProcessorMicroservice
        val messageProcessorConnection = biddingProcessorSocket.accept()
        println("S-a conectat MessageProcessorMicroservice!")

        val bufferReader = BufferedReader(InputStreamReader(messageProcessorConnection.inputStream))

        // se creeaza obiectul Observable cu care se captureaza mesajele de la MessageProcessorMicroservice
        receiveProcessedBidsObservable = Observable.create { emitter ->
            while (true) {
                // se citeste mesajul de la MessageProcessorMicroservice de pe socketul TCP
                val receivedMessage = bufferReader.readLine()

                // daca se primeste un mesaj gol (NULL), cealalta parte a fost inchisa
                if (receivedMessage == null) {
                    bufferReader.close()
                    messageProcessorConnection.close()
                    emitter.onError(Exception("Eroare: MessageProcessorMicroservice ${messageProcessorConnection.port} a fost deconectat."))
                    break
                }

                // daca mesajul este "final", se emite semnalul Complete
                if (Message.deserialize(receivedMessage.toByteArray()).body == "final") {
                    emitter.onComplete()

                    // s-au primit toate mesajele, trimitem confirmare
                    val senderId = "${messageProcessorConnection.localAddress}:${messageProcessorConnection.localPort}"
                    val finishedBidsMessage = Message.create(senderId, "am primit tot")

                    messageProcessorConnection.getOutputStream().write(finishedBidsMessage.serialize())
                    messageProcessorConnection.getOutputStream().flush()
                    messageProcessorConnection.close()
                    break
                } else {
                    // se emite ce s-a citit in fluxul de mesaje
                    emitter.onNext(receivedMessage)
                }
            }
        }
    }

    private fun receiveProcessedBids() {
        // se primesc si se adauga in coada ofertele procesate de la MessageProcessorMicroservice
        val receiveProcessedBidsSubscription = receiveProcessedBidsObservable.subscribeBy(
            onNext = {
                val message = Message.deserialize(it.toByteArray())
                println(message)
                processedBidsQueue.add(message)
            },
            onComplete = {
                // s-a incheiat primirea tuturor mesajelor
                // se decide castigatorul licitatiei
                decideAuctionWinner()
            },
            onError = { println("Eroare: $it") }
        )
        subscriptions.add(receiveProcessedBidsSubscription)
    }

    private fun decideAuctionWinner() {
        // se calculeaza castigatorul ca fiind cel care a ofertat cel mai mult
        val winner: Message? = processedBidsQueue.toList().maxByOrNull {
            // corpul mesajului e de forma "licitez <SUMA_LICITATA>"
            // se preia a doua parte, separata de spatiu
            it.body.split(" ")[1].toInt()
        }

        println("Castigatorul este: ${winner?.sender} cu oferta: ${winner?.body}")

        try {
            auctioneerSocket = Socket(AUCTIONEER_HOST, AUCTIONEER_PORT)

            // se trimite castigatorul catre AuctioneerMicroservice
            if (winner != null) {
                auctioneerSocket.getOutputStream().write(winner.serialize())
                auctioneerSocket.getOutputStream().flush()
                println("Am anuntat castigatorul catre AuctioneerMicroservice.")
            } else {
                println("Nu a existat niciun castigator (lista vida).")
                // Trimit mesaj de eroare sau gol catre Auctioneer ca sa se deblocheze
                val emptyWinnerMessage = Message.create("BiddingProcessor", "licitez 0")
                auctioneerSocket.getOutputStream().write(emptyWinnerMessage.serialize())
                auctioneerSocket.getOutputStream().flush()
            }

            auctioneerSocket.close()
        } catch (e: Exception) {
            println("Nu ma pot conecta la Auctioneer! -> ${e.message}")
            biddingProcessorSocket.close()
            exitProcess(1)
        } finally {
            // Curatam resursele DUPA ce toata executia licitatiei a luat sfarsit
            subscriptions.dispose()
        }
    }

    fun run() {
        receiveProcessedBids()
    }
}

fun main(args: Array<String>) {
    val biddingProcessorMicroservice = BiddingProcessorMicroservice()
    biddingProcessorMicroservice.run()
}