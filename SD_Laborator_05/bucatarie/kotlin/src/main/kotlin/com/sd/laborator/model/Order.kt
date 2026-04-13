package com.sd.laborator.model
import  com.fasterxml.jackson.annotation.JsonProperty

data class Order(
    @JsonProperty("order_id") val orderId: Int,
    @JsonProperty("chelner_id") val  chelnerId: String,
    @JsonProperty("bucatar_id") val bucatarId: String?,
    @JsonProperty("status") val status: String = "PENDING",
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
    )
