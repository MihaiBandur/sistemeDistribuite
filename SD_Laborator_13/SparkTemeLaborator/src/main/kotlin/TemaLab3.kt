package com.sd.laborator

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.api.java.Optional
import org.apache.spark.api.java.function.Function2
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaInputDStream
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies
import scala.Tuple2

fun main(args: Array<String>){
    val sparkConf = SparkConf().setMaster("local[*]").setAppName("TopCuvinte")
    val sparkContext = JavaStreamingContext(sparkConf, Durations.seconds(3))


    sparkContext.checkpoint("src/main/resources/checkpoint_dir")

    val kafkaParams = mapOf(
        "bootstrap.servers" to "localhost:9092",
        "key.deserializer" to StringDeserializer::class.java,
        "value.deserializer" to StringDeserializer::class.java,
        "group.id" to "grup_top_cuvinte_1",
        "auto.offset.reset" to "latest",
        "enable.auto.commit" to false
    )

    val topics = listOf("cuvinte-topic")

    val stream: JavaInputDStream<ConsumerRecord<String, String>> = KafkaUtils.createDirectStream(
        sparkContext,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(topics, kafkaParams)
    )

    val cuvinteStream = stream.map { record -> record.value() }

    val perechiCuvinte = cuvinteStream.mapToPair { cuvant -> Tuple2(cuvant,1) }

    val updateFunction = Function2<List<Int>, Optional<Int>, Optional<Int>>{ valoriNoi, stareVeche ->
        val sumaNoua = valoriNoi.sum()
        val sumaVeche = stareVeche.orElse(0)
        Optional.of(sumaNoua + sumaVeche)
    }


    val istoricCuvant = perechiCuvinte.updateStateByKey(updateFunction)

    istoricCuvant.foreachRDD { rdd, time ->
        println("========= Top 15 Cuvinte la timpul $time =========")

        if(!rdd.isEmpty){
            val rddInvert = rdd.mapToPair { tuple2 -> Tuple2(tuple2._2, tuple2._1) }

            val rddSortatDescrestor = rddInvert.sortByKey(false)

            val top15 = rddSortatDescrestor.take(15)
            for (elem in top15){
                println("${elem._2} : ${elem._1}")
            }
        }else{
            println("Niciun cuvnat primit în acest interval")
        }
        println("################################################\n")
    }

    sparkContext.start()
    sparkContext.awaitTermination()

}