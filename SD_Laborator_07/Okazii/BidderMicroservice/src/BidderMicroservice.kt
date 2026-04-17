import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import kotlin.random.Random
import kotlin.system.exitProcess

class BidderMicroservice {
    private var auctioneerSocket: Socket
    private var auctionResultObservable: Observable<String>
    private var myIdentity: String = "[BIDDER_NECONECTAT]"

    companion object Constants {
        const val AUCTIONEER_HOST = "localhost"
        const val AUCTIONEER_PORT = 1500
        const val MAX_BID = 10_000
        const val MIN_BID = 1_000
    }

    init {
        try {
            auctioneerSocket = Socket(AUCTIONEER_HOST, AUCTIONEER_PORT)
            myIdentity = "[${auctioneerSocket.localPort}]"
            println("$myIdentity M-am conectat la Auctioneer!")

            // Se creeaza un obiect Observable ce va emite mesaje primite printr-un TCP
            auctionResultObservable = Observable.create { emitter ->
                try {
                    // Se citeste raspunsul de pe socketul TCP
                    val bufferReader = BufferedReader(InputStreamReader(auctioneerSocket.inputStream))
                    val receivedMessage = bufferReader.readLine()

                    // Daca se primeste un mesaj gol (NULL), cealalta parte a inchis conexiunea
                    if (receivedMessage == null) {
                        emitter.onError(Exception("AuctioneerMicroservice s-a deconectat."))
                    } else {
                        // Mesajul primit este emis in flux
                        emitter.onNext(receivedMessage)
                        // Se emite semnalul de incheiere al fluxului
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
            // Initializam variabilele pentru a calma compilatorul (chiar daca aplicatia se opreste)
            throw RuntimeException(e)
        }
    }

    private fun bid() {
        // Se genereaza o oferta aleatorie din partea bidderului curent
        val pret = Random.nextInt(MIN_BID, MAX_BID)

        // Formam string-ul cu adresa pe o singura linie pentru a nu avea erori
        val senderId = "${auctioneerSocket.localAddress}:${auctioneerSocket.localPort}"

        // Se creeaza mesajul care incapsuleaza oferta
        val biddingMessage = Message.create(senderId, "licitez $pret")

        // Bidder-ul trimite pretul pentru care doreste sa liciteze
        val serializedMessage = biddingMessage.serialize()
        auctioneerSocket.getOutputStream().write(serializedMessage)
        auctioneerSocket.getOutputStream().flush()

        println("$myIdentity Am trimis oferta: $pret")

        // Exista o sansa din 2 ca bidder-ul sa-si trimita oferta de 2 ori (comentat default)
        // if (Random.nextBoolean()) {
        //    auctioneerSocket.getOutputStream().write(serializedMessage)
        //    auctioneerSocket.getOutputStream().flush()
        // }
    }

    private fun waitForResult() {
        println("$myIdentity Astept rezultatul licitatiei...")

        // Bidder-ul se inscrie pentru primirea unui raspuns la oferta trimisa de acesta
        val auctionResultSubscription = auctionResultObservable.subscribeBy(
            onNext = {
                val resultMessage: Message = Message.deserialize(it.toByteArray())
                println("$myIdentity Rezultat licitatie: ${resultMessage.body}")
            },
            onError = {
                println("$myIdentity Eroare la asteptarea rezultatului: ${it.message}")
            }
        )
        // Se elibereaza memoria obiectului Subscription
        auctionResultSubscription.dispose()
    }

    fun run() {
        bid()
        waitForResult()
    }
}

fun main(args: Array<String>) {
    val bidderMicroservice = BidderMicroservice()
    bidderMicroservice.run()
}