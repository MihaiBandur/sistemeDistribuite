package com.sd.laborator.components

import com.sd.laborator.interfaces.LibraryDAO
import com.sd.laborator.interfaces.LibraryPrinter
import com.sd.laborator.model.Book
import com.sd.laborator.model.Content
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.Exception
import kotlin.concurrent.timer

@Component
class LibraryAppComponent {
    @Autowired
    private lateinit var libraryDAO: LibraryDAO

    @Autowired
    private lateinit var libraryPrinter: LibraryPrinter

    @Autowired
    private lateinit var connectionFactory: RabbitMqConnectionFactoryComponent
    private lateinit var amqpTemplate: AmqpTemplate

    @Autowired
    fun initTemplate() {
        this.amqpTemplate = connectionFactory.rabbitTemplate()
    }

    fun sendMessage(msg: String) {
        this.amqpTemplate.convertAndSend(connectionFactory.getExchange(),
                                         connectionFactory.getRoutingKey(),
                                         msg)
    }

    @RabbitListener(queues = ["\${libraryapp.rabbitmq.queue}"])
    fun recieveMessage(msg: String) {
        val processedMsg = (msg.split(",").map { it.toInt().toChar() }).joinToString(separator = "")
        try {

            val parts = processedMsg.split(":", limit = 3)
            val function = parts.getOrNull(0) ?: ""

            val result: String? = when (function) {
                "print" -> customPrint(parts.getOrNull(1) ?: "json")
                "find" -> customFind(parts.getOrNull(1) ?: "", parts.getOrNull(2) ?: "json")
                "add" -> {
                    val payload = processedMsg.removePrefix("add:")
                    customAdd(payload)
                }
                else -> null
            }

            if(result != null) sendMessage(result)

        } catch (e: Exception) {
            println(e)
        }
    }

    fun customAdd(payload: String): String {
        try {
            val props = payload.split("|").associate {
                val kv = it.split("=", limit = 2)
                kv[0] to (if (kv.size > 1) kv[1] else "")
            }

            val title = props["title"] ?: ""
            val author = props["author"] ?: ""
            val publisher = props["publisher"] ?: ""
            val text = props["text"] ?: ""

            if (title.isBlank() || author.isBlank()) {
                return "Eroare: Titlul și autorul sunt campuri obligatorii!"
            }


            val content = Content(author, text, title, publisher)
            val book = Book(content)

            val success = this.addBook(book)

            return if (success) {
                "SUCCESS: Cartea '$title' a fost adaugata în biblioteca!"
            } else {
                "Eroare: Nu s-a putut salva cartea în baza de date."
            }
        } catch (e: Exception) {
            return "Eroare la procesarea datelor trimise din Python."
        }
    }



    fun customPrint(format: String): String {
        return when(format) {
            "html" -> libraryPrinter.printHTML(libraryDAO.getBooks())
            "json" -> libraryPrinter.printJSON(libraryDAO.getBooks())
            "raw" -> libraryPrinter.printRaw(libraryDAO.getBooks())
            "xml" -> libraryPrinter.printXML(libraryDAO.getBooks())
            else -> "Not implemented"
        }
    }

    fun customFind(searchParameter: String, format: String): String {
        val (field, value) = searchParameter.split("=")

        val foundBooks = when(field){
            "author" -> this.libraryDAO.findAllByAuthor(value)
            "title" -> this.libraryDAO.findAllByTitle(value)
            "publisher" -> this.libraryDAO.findAllByPublisher(value)
            else -> emptySet()
        }

        return when(format){
            "html" -> this.libraryPrinter.printHTML(foundBooks)
            "json" -> this.libraryPrinter.printJSON(foundBooks)
            "raw" -> this.libraryPrinter.printRaw(foundBooks)
            "xml" -> this.libraryPrinter.printXML(foundBooks)
            else -> "Format necunoscut"
        }
    }

    fun addBook(book: Book): Boolean {
        return try {
            this.libraryDAO.addBook(book)
            true
        } catch (e: Exception) {
            false
        }
    }

}