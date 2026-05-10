package com.sd.laborator

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional

@Serdeable
data class ClickEvent(val buttonName: String)

@MappedEntity("button_stats")
data class ButtonStat(
    @field:Id @field:GeneratedValue var id: Long? = null,
    var buttonName: String,
    var clicks: Int = 0
)

@JdbcRepository(dialect = Dialect.MYSQL)
interface ButtonStatRepository : CrudRepository<ButtonStat, Long> {
    fun findByButtonName(buttonName: String): Optional<ButtonStat>
}