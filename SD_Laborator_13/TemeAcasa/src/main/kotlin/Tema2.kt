package com.sd.laborator

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.api.java.Optional
import org.apache.spark.api.java.function.Function2
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.*
import org.json.JSONObject
import scala.Tuple2
import java.io.Serializable

class Stats(
    val sumX: Double,
    val sumX2: Double,
    val count: Long
) : Serializable {
    constructor() : this(0.0, 0.0, 0)
}

fun main(args: Array<String>){
    val sparkConf = SparkConf().setMaster("local[*]").setAppName("AnalizaDispersie")
    val sparkContext = JavaStreamingContext(sparkConf, Durations.seconds(5))
    sparkContext.checkpoint("src/main/resources/checkpoint")

    val kafkaParams = mapOf(
        "bootstrap.servers" to "localhost:9092",
        "key.deserializer" to StringDeserializer::class.java,
        "value.deserializer" to StringDeserializer::class.java,
        "group.id" to "analiza_grup",
        "auto.offset.reset" to "latest"
    )

    val topics = listOf("mouse-topic", "random-topic")

    val stream = KafkaUtils.createDirectStream<String, String>(
        sparkContext,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(topics, kafkaParams)
    )

    val statsStream = stream.mapToPair { record ->
        val json = JSONObject(record.value())
        val tip = json.getString("tip")
        val x = if(tip == "mouse") json.getDouble("x") else json.getDouble("rand_x")
        Tuple2(tip, Stats(x, x * x, 1))
    }


    val updateFunction = Function2<List<Stats>, Optional<Stats>, Optional<Stats>>{values, state ->
        val old = state.orElse(Stats(0.0,0.0,0))
        var newSumX = old.sumX
        var newSumX2 = old.sumX2
        var newCount = old.count

        for(v in values){
            newSumX += v.sumX
            newSumX2 += v.sumX2
            newCount += v.count
        }
        Optional.of(Stats(newSumX, newSumX2, newCount))
    }

    val stateStream = statsStream.updateStateByKey(updateFunction)

    stateStream.foreachRDD { rdd ->
        rdd.foreach { tuple ->
            val tip = tuple._1
            val stats = tuple._2
            if (stats.count > 1) {
                val mean = stats.sumX / stats.count // Corectat: sumX/count
                val variance = (stats.sumX2 / stats.count) - (mean * mean)
                println("Flux: $tip | Dispersie: ${"%.2f".format(variance)} | Nr. puncte: ${stats.count}")
            }
        }
    }

    sparkContext.start()
    sparkContext.awaitTermination()
}
