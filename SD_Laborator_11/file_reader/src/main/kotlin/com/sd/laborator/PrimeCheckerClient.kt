package com.sd.laborator
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
@Client("http://localhost:8080")
interface PrimeCheckerClient {
    @Post
    fun sendNumbersToCheck(@Body eratosteneRequest: EratosteneRequest): EratosteneResponse
}