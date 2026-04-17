import java.text.SimpleDateFormat
import java.util.*

class Message private constructor(
    val sender: String,
    val body: String,
    val timestamp: Date
) {
    companion object {
        fun create(sender: String, body: String): Message {
            return Message(sender, body, Date())
        }

        fun deserialize(msg: ByteArray): Message {
            // Folosim trim() pentru a elimina '\n' adăugat la serializare
            val msgString = String(msg).trim()
            val (timestamp, sender, body) = msgString.split(' ', limit = 3)
            return Message(sender, body, Date(timestamp.toLong()))
        }
    }

    fun serialize(): ByteArray {
        return "${timestamp.time} $sender $body\n".toByteArray()
    }

    override fun toString(): String {
        // Am pus șirul de formatare pe o singură linie
        val dateString = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp)
        return "[$dateString] $sender >>> $body"
    }
}

fun main(args: Array<String>) {
    val msg = Message.create("localhost:4848", "test mesaj")
    println(msg)

    val serialized = msg.serialize()
    val deserialized = Message.deserialize(serialized)
    println(deserialized)
}