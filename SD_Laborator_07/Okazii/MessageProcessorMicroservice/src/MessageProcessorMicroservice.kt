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

class MessageProcessorMicroservice {
    private var messageProcessorSocket: ServerSocket
    private lateinit var biddingProcessorSocket: Socket
    private var auctioneerConnection: Socket
    private var receiveInQueueObservable: Observable<String>
    private val subscriptions = CompositeDisposable()
    private val messageQueue: Queue<Message> = LinkedList()


    private val journal = ExecutionJournal("message_processor")

    companion object Constants {
        const val MESSAGE_PROCESSOR_PORT   = 1600
        const val BIDDING_PROCESSOR_HOST   = "localhost"
        const val BIDDING_PROCESSOR_PORT   = 1700
    }

    init {
        messageProcessorSocket = ServerSocket(MESSAGE_PROCESSOR_PORT)
        println("MessageProcessorMicroservice se executa pe portul: ${messageProcessorSocket.localPort}")
        println("Se asteapta mesaje pentru procesare...")


        val unfinished = journal.getUnfinishedOperations()
        if (unfinished.isNotEmpty()) {
            println("MessageProcessor: detectate operații neterminate – se reia procesarea...")
            unfinished.forEach { (opId, data) ->
                when {
                    opId.startsWith("enqueue_") -> {
                        try {
                            val msg = Message.deserialize(data.toByteArray())
                            messageQueue.add(msg)
                            journal.logEnd(opId)
                            println(" Mesaj recuperat în coadă: $msg")
                        } catch (e: Exception) {
                            println(" Nu s-a putut recupera mesajul '$opId': ${e.message}")
                        }
                    }
                    opId.startsWith("send_to_bidding_") -> {
                        println(" Trimitere spre BiddingProcessor întreruptă – va fi reluată.")
                    }
                }
            }
        }


        auctioneerConnection = messageProcessorSocket.accept()
        println("S-a conectat AuctioneerMicroservice!")

        val bufferReader = BufferedReader(InputStreamReader(auctioneerConnection.inputStream))

        receiveInQueueObservable = Observable.create { emitter ->
            while (true) {
                val receivedMessage = bufferReader.readLine()

                if (receivedMessage == null) {
                    bufferReader.close()
                    auctioneerConnection.close()
                    emitter.onError(Exception("Eroare: AuctioneerMicroservice ${auctioneerConnection.port} a fost deconectat."))
                    break
                }

                if (Message.deserialize(receivedMessage.toByteArray()).body == "final") {
                    emitter.onComplete()
                    break
                } else {
                    emitter.onNext(receivedMessage)
                }
            }
        }
    }

    private fun receiveAndProcessMessages() {
        val receiveInQueueSubscription = receiveInQueueObservable
            .distinct()
            .subscribeBy(
                onNext = {
                    val message = Message.deserialize(it.toByteArray())
                    val opId    = "enqueue_${message.senderIpPort}_${message.timestamp.time}"

                    journal.logStart(opId, it.trim())
                    println("Mesaj valid retinut $message")
                    messageQueue.add(message)
                    journal.logEnd(opId)
                },
                onComplete = {
                    // Sortare după timestamp, curățare și re-populare coadă
                    val sorted = messageQueue.sortedBy { it.timestamp }
                    messageQueue.clear()
                    messageQueue.addAll(sorted)

                    // Confirmăm primirea tuturor mesajelor spre Auctioneer
                    val senderId               = "${auctioneerConnection.localAddress}:${auctioneerConnection.localPort}"
                    val finishedMessagesMessage = Message.create(senderId, "am primit tot")
                    auctioneerConnection.getOutputStream().write(finishedMessagesMessage.serialize())
                    auctioneerConnection.getOutputStream().flush()
                    auctioneerConnection.close()

                    sendProcessedMessages()
                },
                onError = { println("Eroare: $it") }
            )
        subscriptions.add(receiveInQueueSubscription)
    }

    private fun sendProcessedMessages() {
        val sendOpId = "send_to_bidding_${System.currentTimeMillis()}"
        journal.logStart(sendOpId, "trimitere_spre_bidding_processor")

        try {
            biddingProcessorSocket = Socket(BIDDING_PROCESSOR_HOST, BIDDING_PROCESSOR_PORT)
            println("Trimit urmatoarele mesaje:")

            val forwardSubscription = Observable.fromIterable(messageQueue).subscribeBy(
                onNext = {
                    val itemOpId = "send_item_${it.senderIpPort}_${it.timestamp.time}"
                    journal.logStart(itemOpId, String(it.serialize()).trim())

                    println(it.toString())
                    biddingProcessorSocket.getOutputStream().write(it.serialize())
                    biddingProcessorSocket.getOutputStream().flush()

                    journal.logEnd(itemOpId)
                },
                onComplete = {
                    val senderId      = "${biddingProcessorSocket.localAddress}:${biddingProcessorSocket.localPort}"
                    val noMoreMessages = Message.create(senderId, "final")

                    biddingProcessorSocket.getOutputStream().write(noMoreMessages.serialize())
                    biddingProcessorSocket.getOutputStream().flush()
                    biddingProcessorSocket.close()

                    journal.logEnd(sendOpId)
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
        startHeartbeat("MessageProcessorMicroservice")
        receiveAndProcessMessages()
    }
}

fun main(args: Array<String>) {
    val messageProcessorMicroservice = MessageProcessorMicroservice()
    messageProcessorMicroservice.run()
}