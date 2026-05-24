package com.sd.laborator

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.*

fun main(array: Array<String>){
    val spark = SparkSession.builder()
        .appName("CharHistogramSQL")
        .config("spark.master", "local[*]")
        .orCreate

    val df = spark.read().text("src/main/resources/5000-8.txt")

    val histogramDf = df
        .select(explode(split(lower(col("value")), "")).`as`("char"))
        .filter(col("char").rlike("[a-z]"))
        .groupBy("char")
        .count()
        .orderBy("char")

    println("####### HISTOGRAMA SQL #######")
    histogramDf.show(26)

    spark.stop()
}