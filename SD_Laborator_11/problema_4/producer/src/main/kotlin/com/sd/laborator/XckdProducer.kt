package com.sd.laborator

import io.micronaut.function.FunctionBean
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.function.Supplier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@FunctionBean("xckd-producer")
class XckdProducer: Supplier<String> {
    private val LOG = LoggerFactory.getLogger(XckdProducer::class.java)
    override fun get(): String {
        LOG.info("Producatorul a fost activat")
        val xmlContent = URL("https://www.reddit.com/r/programming/.rss").readText()

        LOG.info("Trimit datele catre consumator ")

        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/"))
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(xmlContent))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return "Proces finalizat! Status răspuns consumator: ${response.statusCode()}"
    }


}
