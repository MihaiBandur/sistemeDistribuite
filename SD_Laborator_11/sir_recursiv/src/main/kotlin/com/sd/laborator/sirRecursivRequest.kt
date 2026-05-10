package com.sd.laborator
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Deserializable
class sirRecursivRequest {

    var n: Int = 0

    fun get_number(): Int = n


}