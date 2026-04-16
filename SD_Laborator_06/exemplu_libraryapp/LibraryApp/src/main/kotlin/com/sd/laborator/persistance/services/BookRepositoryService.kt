package com.sd.laborator.persistance.services

import com.sd.laborator.business.models.Book
import com.sd.laborator.persistance.interfaces.IBookRepository
import com.sd.laborator.persistance.mapper.BookRowMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct
@Repository
class BookRepositoryService: IBookRepository {
    @Autowired
    private lateinit var  _jdbcTemplate: JdbcTemplate

    private  var _rowMapper: RowMapper<Book?> = BookRowMapper()
    @PostConstruct
    override fun createTable() {
        _jdbcTemplate.execute ("""CREATE TABLE IF NOT EXISTS books(
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        author VARCHAR(100),
                                        title VARCHAR(100),
                                        publisher VARCHAR(100),
                                        text VARCHAR(100))""")
    }

    override fun add(book: Book) {
        try {
            val author: String = book.author ?: "Autor necunoscut"
            val title: String = book.title ?: "Titlu necunoscut"
            val publisher: String = book.publisher ?: "Editura necunoscuta"
            val text: String = book.content ?: "Continut necunoscut"
            _jdbcTemplate.update("INSERT INTO books(author, title, publisher, text) VALUES (?, ?, ?, ?)", author, title, publisher, text)
        }catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.add")
        }
    }

    override fun getAll(): List<Book?> {
        return _jdbcTemplate.query("SELECT * FROM books", _rowMapper)
    }

    override fun getByAuthor(author: String): List<Book?> {
        return try {
            _jdbcTemplate.query("SELECT * FROM books WHERE author = ?", _rowMapper, author)
        }catch (e: EmptyResultDataAccessException){
            emptyList()
        }
    }

    override fun getByPublisher(publisher: String): List<Book?> {
        return try {
            _jdbcTemplate.query("SELECT * FROM books WHERE publisher = ?", _rowMapper, publisher)
        }catch (e: EmptyResultDataAccessException){
            emptyList()
        }
    }

    override fun getByTitle(title: String): List<Book?> {
        return try {
            _jdbcTemplate.query("SELECT * FROM books WHERE title = ?", _rowMapper, title)
        }catch (e: EmptyResultDataAccessException){
            emptyList()
        }
    }

    override fun delete(idSearcBook: Int) {
        try {
            _jdbcTemplate.update("DELETE FROM books WHERE id = ?",idSearcBook )
        }catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.delete")
        }
    }

    override fun update(idSearchBook: Int, book: Book) {
        try {
            val author: String = book.author ?: "Autor necunoscut"
            val title: String = book.title ?: "Titlu necunoscut"
            val publisher: String = book.publisher ?: "Editura necunoscuta"
            val text: String = book.content ?: "Continut necunoscut"
            _jdbcTemplate.update("UPDATE books SET author = ?, title = ?, publisher = ?, text = ? WHERE id = ?", author,title, publisher, text, idSearchBook)
        }catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.update")
        }
    }

}