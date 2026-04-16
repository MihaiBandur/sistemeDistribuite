package com.sd.laborator.persistance.mapper

import com.sd.laborator.business.models.Book
import com.sd.laborator.business.models.Content
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.SQLException

class BookRowMapper: RowMapper<Book?>  {
    @Throws(SQLException::class)
    override fun mapRow(rs: ResultSet, rowNum: Int): Book? {
        return Book(Content(rs.getInt("id"), rs.getString("author"), rs.getString("text"), rs.getString("title"), rs.getString("publisher")))

    }
}