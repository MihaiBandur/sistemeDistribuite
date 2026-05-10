package com.sd.laborator

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Deserializable
class EratosteneRequest {
    var number: Int = 0

    fun get_Number(): Int = number
}