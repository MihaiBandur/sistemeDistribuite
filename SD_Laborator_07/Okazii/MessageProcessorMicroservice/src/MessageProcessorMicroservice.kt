import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.system.exitProcess

class MessageProcessorMicroservice {
    private var messageProcessorSocket: ServerSocket
    private lateinit var biddingProcessorSocket: Socket
    private var auctioneerConnection: Socket
    private var receiveInQueueObservable: Observable<String>
    private val subscriptions = CompositeDisposable()
    private val messageQueue: Queue<Message> = LinkedList<Message>()

    companion object Constants {
        const val MESSAGE_PROCESSOR_PORT = 1600
        const val BIDDING_PROCESSOR_HOST = "localhost"
        const val BIDDING_PROCESSOR_PORT = 1700
    }

    init {
        messageProcessorSocket = ServerSocket(MESSAGE_PROCESSOR_PORT)
        println("MessageProcessorMicroservice se executa pe portul: ${messageProcessorSocket.localPort}")
        println("Se asteapta mesaje pentru procesare...")

        // Se asteapta conectarea de la AuctioneerMicroservice
        auctioneerConnection = messageProcessorSocket.accept()
        println("S-a conectat AuctioneerMicroservice!")

        val bufferReader = BufferedReader(InputStreamReader(auctioneerConnection.inputStream))

        // se creeaza obiectul Observable cu care se captureaza mesajele de la AuctioneerMicroservice
        receiveInQueueObservable = Observable.create { emitter ->
            while (true) {
                // se citeste mesajul de la AuctioneerMicroservice de pe socketul TCP
                val receivedMessage = bufferReader.readLine()

                // daca se primeste un mesaj gol (NULL), cealalta parte a fost inchisa
                if (receivedMessage == null) {
                    bufferReader.close()
                    auctioneerConnection.close()
                    emitter.onError(Exception("Eroare: AuctioneerMicroservice ${auctioneerConnection.port} a fost deconectat."))
                    break
                }

                // daca mesajul este cel de incheiere a licitatiei (avand corpul "final"), se emite Complete
                if (Message.deserialize(receivedMessage.toByteArray()).body == "final") {
                    emitter.onComplete()
                    break
                } else {
                    // se emite ce s-a citit ca si element in fluxul de mesaje
                    emitter.onNext(receivedMessage)
                }
            }
        }
    }

    private  fun receiveAndProcessMessages(){
        val receiveInQueueSubscription = receiveInQueueObservable
            .distinct()
            .subscribeBy(
                onNext ={
                    val message = Message.deserialize(it.toByteArray())
                    println("Mesaj valid retinut $message")
                    messageQueue.add(message)
                } ,
                onComplete = {
                    val sortedList = messageQueue.sortedBy { it.timestamp }
                    messageQueue.clear()
                    messageQueue.addAll(sortedList)

                    val senderId = "${auctioneerConnection.localAddress}:${auctioneerConnection.localPort}"
                    val finishedMessagesMessage = Message.create(senderId, "am primit tot")

                    auctioneerConnection.getOutputStream().write(finishedMessagesMessage.serialize())
                    auctioneerConnection.getOutputStream().flush()
                    auctioneerConnection.close()

                    sendProcessedMessages()
                },
                onError = {
                        println("Eroare: $it")
                }

            )
        subscriptions.add(receiveInQueueSubscription)
    }
    private fun sendProcessedMessages() {
        try {
            biddingProcessorSocket = Socket(BIDDING_PROCESSOR_HOST, BIDDING_PROCESSOR_PORT)
            println("Trimit urmatoarele mesaje:")

            val forwardSubscription = Observable.fromIterable(messageQueue).subscribeBy(
                onNext = {
                    println(it.toString())
                    // trimitere mesaje catre procesorul licitatiei, care decide rezultatul final
                    biddingProcessorSocket.getOutputStream().write(it.serialize())
                    biddingProcessorSocket.getOutputStream().flush()
                },
                onComplete = {
                    val senderId = "${biddingProcessorSocket.localAddress}:${biddingProcessorSocket.localPort}"
                    val noMoreMessages = Message.create(senderId, "final")

                    biddingProcessorSocket.getOutputStream().write(noMoreMessages.serialize())
                    biddingProcessorSocket.getOutputStream().flush()
                    biddingProcessorSocket.close()

                    // se elibereaza memoria din multimea de Subscriptions
                    subscriptions.dispose()
                }
            )
            subscriptions.add(forwardSubscription)
        } catch (e: Exception) {
            println("Nu ma pot conecta la BiddingProcessor!")
            messageProcessorSocket.close()
            exitProcess(1)
        }
    }

    fun run() {
        receiveAndProcessMessages()
    }
}

fun main(args: Array<String>) {
    val messageProcessorMicroservice = MessageProcessorMicroservice()
    messageProcessorMicroservice.run()
}