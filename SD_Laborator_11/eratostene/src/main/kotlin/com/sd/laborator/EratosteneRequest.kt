package com.sd.laborator

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Deserializable
class EratosteneRequest {
    var number: Int = 0
    var numbersToCheck: List<Int> = emptyList()

    fun get_Number(): Int = number

    fun get_numbersToCheck(): List<Int> = numbersToCheck
}