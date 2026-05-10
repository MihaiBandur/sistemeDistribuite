package com.sd.laborator

import  io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Deserializable
class Book {
    var name: String? = null
}