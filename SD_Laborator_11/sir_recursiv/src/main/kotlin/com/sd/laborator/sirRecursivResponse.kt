package com.sd.laborator

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Serializable
class sirRecursivResponse {
    private var target_number: Long = 0
    private var message: String = ""

    fun getTargetNumber(): Long = target_number
    fun getMessage(): String = message

    fun setTargetNumber(target_number: Long){
        this.target_number = target_number
    }

    fun setMessage(message: String){
        this.message = message
    }
}