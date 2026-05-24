package com.sd.laborator

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaInputDStream
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies
import org.json.JSONObject

fun main(args: Array<String>){
    val sparkConf = SparkConf().setMaster("local[*]").setAppName("KafkaDualConsumer")
    val sparkContext = JavaStreamingContext(sparkConf, Durations.seconds(2))

    val kafkaParams = mapOf(
        "bootstrap.servers" to "localhost:9092",
        "key.deserializer" to StringDeserializer::class.java,
        "value.deserializer" to StringDeserializer::class.java,
        "group.id" to "group_mouse_aleator",
        "auto.offset.reset" to "latest"
    )

    val mouse_stream: JavaInputDStream<ConsumerRecord<String, String>> = KafkaUtils.createDirectStream(
        sparkContext,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(listOf("mouse-topic"), kafkaParams)
    )


    val random_stream: JavaInputDStream<ConsumerRecord<String, String>> = KafkaUtils.createDirectStream(
        sparkContext,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(listOf("random-topic"), kafkaParams)
    )

    val combined_stream = mouse_stream.union(random_stream)

    combined_stream.map { record ->
        val json = JSONObject(record.value())
        val tip = json.getString("tip")
        if (tip == "mouse") {
            "MOUSE: X=${json.get("x")}, Y=${json.get("y")}"
        } else {
            "ALEATOR: X=${json.get("rand_x")}, Y=${json.get("rand_y")}"
    } }.print()

    sparkContext.start()

    sparkContext.awaitTermination()


}