package com.sd.laborator
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

@MappedEntity("click_counter")
class ClickCounter(
    @field:Id var id: Long = 1,
    var clicks: Int = 0
)

@JdbcRepository(dialect = Dialect.MYSQL)
interface CounterRepository: CrudRepository<ClickCounter, Long>