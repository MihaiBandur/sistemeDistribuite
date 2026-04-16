package com.sd.laborator.business.interfaces

import com.sd.laborator.business.models.Book

interface ILibraryDAOService {
    fun createTable()
    fun getBooks(): Set<Book>
    fun addBook(book: Book)
    fun findAllByAuthor(author: String): Set<Book>
    fun findAllByTitle(title: String): Set<Book>
    fun findAllByPublisher(publisher: String): Set<Book>
    fun updateBook(idSearchBook: Int, book: Book)
    fun deleteBook(idSearchBook: Int)
}