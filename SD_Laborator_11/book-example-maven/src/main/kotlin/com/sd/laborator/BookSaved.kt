package com.sd.laborator
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Serializable
class BookSaved {
    var name: String? = null
    var isbn: String? = null
}