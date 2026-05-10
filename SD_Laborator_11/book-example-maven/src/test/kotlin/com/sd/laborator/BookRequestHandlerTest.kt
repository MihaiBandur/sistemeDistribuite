package com.sd.laborator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
class BookRequestHandlerTest {
    @Test
    fun testHandler(){
        val bookRequestHandler: BookRequestHandler = BookRequestHandler()
        val book = Book()
        book.name = "Building Microservice"
        val bookSaved = bookRequestHandler.execute(book)
        Assertions.assertNotNull(bookSaved)
        Assertions.assertEquals(book.name, bookSaved!!.name)
        Assertions.assertNotNull(bookSaved.isbn)
        bookRequestHandler.close()
    }
}