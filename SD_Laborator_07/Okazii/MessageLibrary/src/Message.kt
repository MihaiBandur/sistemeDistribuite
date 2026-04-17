import java.text.SimpleDateFormat
import java.util.*

class Message private constructor(
    val senderIpPort: String,
    val nume: String,
    val telefon: String,
    val email: String,
    val body: String,
    val timestamp: Date
) {

    val sender: String get() = senderIpPort

    companion object {

        fun create(senderIpPort: String, body: String): Message {
            return Message(senderIpPort, "", "", "", body, Date())
        }
        fun create(senderIpPort: String, nume: String, telefon: String, email: String, body: String): Message {
            return Message(senderIpPort, nume, telefon, email, body, Date())
        }

        fun deserialize(msg: ByteArray): Message {
            val msgString = String(msg).trim()
            val parts = msgString.split('|', limit = 6)
            if (parts.size < 6) throw IllegalArgumentException("Format mesaj invalid: $msgString")

            return Message(parts[1], parts[2], parts[3], parts[4], parts[5], Date(parts[0].toLong()))
        }
    }

    fun serialize(): ByteArray {
     return   "${timestamp.time}|$senderIpPort|$nume|$telefon|$email|$body\n".toByteArray()
    }

    override fun toString(): String {
        val dateString = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp)
        val identity = if (nume.isNotBlank()) "$nume ($email, $telefon) @ $senderIpPort" else senderIpPort
        return "[$dateString] $identity >>> $body"
    }
}

fun main(args: Array<String>) {
    val msg = Message.create("localhost:4848", "test mesaj")
    println(msg)

    val serialized = msg.serialize()
    val deserialized = Message.deserialize(serialized)
    println(deserialized)
}