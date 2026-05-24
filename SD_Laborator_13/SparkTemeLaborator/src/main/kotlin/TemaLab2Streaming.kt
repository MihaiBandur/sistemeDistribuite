package com.sd.laborator

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaStreamingContext
import scala.Tuple2

fun main(args: Array<String>){
    val sparkConf = SparkConf().setMaster("local[*]").setAppName("CharHistogramStreaming")

    val sparkContext = JavaStreamingContext(sparkConf, Durations.seconds(5))

    val lines = sparkContext.textFileStream("src/main/resources/streaming_input/")

    val charCounts = lines
        .flatMap { l -> l.lowercase().toList().iterator() }
        .filter { c -> c in 'a'..'z' }
        .mapToPair { c-> Tuple2(c, 1) }
        .reduceByKey { a , b->a + b }

    charCounts.print()

    sparkContext.start()
    print("Programul Apache Spark Stream a inceput")
    sparkContext.awaitTermination()

}