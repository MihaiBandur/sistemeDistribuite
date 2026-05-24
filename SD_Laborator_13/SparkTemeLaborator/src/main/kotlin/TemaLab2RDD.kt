package com.sd.laborator

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import scala.Tuple2

fun main(array: Array<String>){
    val sparkConf = SparkConf().setMaster("local[*]").setAppName("CharHistogramRDD")

    val sparkContext = JavaSparkContext(sparkConf)

    val lines = sparkContext.textFile("src/main/resources/5000-8.txt")

    val charCounts = lines
        .flatMap { l -> l.lowercase().toList().iterator() }
        .filter { c -> c in 'a'..'z' }
        .mapToPair { c -> Tuple2(c, 1) }
        .reduceByKey { a,b->a + b }
        .sortByKey()

    println("==== HISTOGRAMA RDD ====")
    charCounts.collect().forEach { println("${it._1} -> ${it._2}") }

    sparkContext.stop()

}