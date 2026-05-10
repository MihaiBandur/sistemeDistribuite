package com.sd.laborator
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable

@Introspected
@Serdeable.Serializable
class EratosteneResponse {
    private var message: String? = null
    private var primesList: List<Int>? = null

    fun getPrimes(): List<Int>? = primesList

    fun setPrimes(primes: List<Int>?){
        this.primesList = primes
    }

    fun getMessage(): String? = message

    fun setMessage(message: String?) {
        this.message = message
    }
}

