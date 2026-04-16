package com.sd.laborator.persistance.interfaces
import com.sd.laborator.business.models.Book
interface IBookRepository {

    fun createTable()

    fun add(book: Book)

    fun getAll(): List<Book?>

    fun getByAuthor(author: String): List<Book?>
    fun getByTitle(title: String): List<Book?>
    fun getByPublisher(publisher: String): List<Book?>

    fun update(idSearchBook: Int, book: Book)

    fun delete(idSearcBook: Int)
}