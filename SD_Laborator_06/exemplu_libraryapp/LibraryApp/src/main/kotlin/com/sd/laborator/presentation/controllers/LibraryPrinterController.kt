package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.ILibraryDAOService
import com.sd.laborator.business.interfaces.ILibraryPrinterService
import com.sd.laborator.business.models.Book
import com.sd.laborator.business.models.Content
import com.sd.laborator.presentation.config.RabbitMqComponent
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LibraryPrinterController {
    @Autowired
    private lateinit var _libraryDAOService: ILibraryDAOService

    @Autowired
    private lateinit var _libraryPrinterService: ILibraryPrinterService

    @Autowired
    private lateinit var _rabbitMqComponent: RabbitMqComponent

    private lateinit var _amqpTemplate: AmqpTemplate

    @Autowired
    fun initTempplate(){
        this._amqpTemplate = _rabbitMqComponent.rabbitTemplate()
    }


    @RequestMapping("/print", method = [RequestMethod.GET])
    @ResponseBody
    fun customPrint(@RequestParam(required = true, name = "format", defaultValue = "") format: String): String {
        return when (format) {
            "html" -> _libraryPrinterService.printHTML(_libraryDAOService.getBooks())
            "json" -> _libraryPrinterService.printJSON(_libraryDAOService.getBooks())
            "raw" -> _libraryPrinterService.printRaw(_libraryDAOService.getBooks())
            else -> "Not implemented"
        }
    }
    @RequestMapping("/add", method = [RequestMethod.GET, RequestMethod.POST])
    @ResponseBody
    fun addBook(
        @RequestParam author: String,
        @RequestParam title: String,
        @RequestParam publisher: String,
        @RequestParam text: String
    ): String{
        val newBook: Book = Book(Content(0, author, text, title, publisher))
        _libraryDAOService.addBook(newBook)
        return "Cartea '$title' de $author a fost adăugată cu succes în baza de date!"

    }
    @RequestMapping("/find-and-print", method = [RequestMethod.GET])
    @ResponseBody
    fun customFind(
        @RequestParam(required = false, name = "author", defaultValue = "") author: String,
        @RequestParam(required = false, name = "title", defaultValue = "") title: String,
        @RequestParam(required = false, name = "publisher", defaultValue = "") publisher: String,
        @RequestParam(required = true, defaultValue = "raw") format: String
    ): String {


        val books = when {
            author.isNotEmpty() -> _libraryDAOService.findAllByAuthor(author)
            title.isNotEmpty() -> _libraryDAOService.findAllByTitle(title)
            publisher.isNotEmpty() -> _libraryDAOService.findAllByPublisher(publisher)
            else -> _libraryDAOService.getBooks() // Dacă nu se dă niciun criteriu, le luăm pe toate
        }


        return when (format.lowercase()) {
            "html" -> _libraryPrinterService.printHTML(books)
            "json" -> _libraryPrinterService.printJSON(books)
            else -> _libraryPrinterService.printRaw(books)
        }
    }

    @RabbitListener(queues = ["\${libraryapp.rabbitmq.queue}"])
    fun fetchMessage(message: String){
        println("Debug Message ~> Message received: $message")
        TODO("implementeaza functia de raspsnu")
    }
    private fun sendMessage(msg: String){
        println("Debug Message ~> Message to send: $msg")
        this._amqpTemplate.convertAndSend(_rabbitMqComponent.getExchange(),_rabbitMqComponent.getRountingKey(), msg)
    }

}