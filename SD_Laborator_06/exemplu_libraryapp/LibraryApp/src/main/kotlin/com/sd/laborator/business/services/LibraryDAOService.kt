package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.ILibraryDAOService
import com.sd.laborator.business.models.Book
import com.sd.laborator.business.models.Content
import com.sd.laborator.persistance.interfaces.IBookRepository
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired



@Service
class LibraryDAOService : ILibraryDAOService {

    @Autowired
    private lateinit var bookRepositoryService: IBookRepository
    override fun createTable() {
        bookRepositoryService.createTable()
    }
    override fun getBooks(): Set<Book> {
        return bookRepositoryService.getAll().filterNotNull().toSet()
    }

    override fun addBook(book: Book) {
        bookRepositoryService.add(book)
    }

    override fun findAllByAuthor(author: String): Set<Book> {
        return bookRepositoryService.getByAuthor(author).filterNotNull().toSet()
    }

    override fun findAllByTitle(title: String): Set<Book> {
        return bookRepositoryService.getByTitle(title).filterNotNull().toSet()
    }

    override fun findAllByPublisher(publisher: String): Set<Book> {
        return bookRepositoryService.getByPublisher(publisher).filterNotNull().toSet()
    }

    override fun updateBook(idSearchBook: Int, book: Book) {
        bookRepositoryService.update(idSearchBook, book)
    }

    override fun deleteBook(idSearchBook: Int) {
        bookRepositoryService.delete(idSearchBook)
    }
}