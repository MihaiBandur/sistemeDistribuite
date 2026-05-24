package com.sd.laborator

import org.apache.hadoop.shaded.org.apache.http.util.Args
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaStreamingContext

fun main(args: Array<String>){
    val sparkConf = SparkConf().setMaster("local[2]").setAppName("NetworkWordFilter")
    val sparkContext = JavaStreamingContext(sparkConf, Durations.seconds(1))

    val cuvinteInterzise = setOf("si", "dar", "sau", "cu", "de", "la")

    val lines = sparkContext.socketTextStream("localhost", 9999)

    val filteredLines = lines.map { line ->
        line.split(" ")
            .filter { word -> word.lowercase() !in cuvinteInterzise }
            .joinToString(" ")
    }

    filteredLines.print()

    sparkContext.start()
    sparkContext.awaitTermination()
}