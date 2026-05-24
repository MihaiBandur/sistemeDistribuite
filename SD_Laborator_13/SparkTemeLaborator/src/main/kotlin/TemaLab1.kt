package com.sd.laborator

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.api.java.function.Function
import org.apache.spark.api.java.function.Function2
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.storage.StorageLevel

internal class GetLength : Function<String?, Int?> {
    override fun call(p0: String?): Int? {
        return p0?.length ?: 0
    }
}

internal class Sum : Function2<Int?, Int?, Int?> {
    override fun call(p0: Int?, p1: Int?): Int? {
        return (p0?.toInt() ?: 0) + (p1?.toInt() ?: 0)
    }
}


fun main(args: Array<String>){
    val sparkConf = SparkConf()
        .setMaster("local[*]")
        .setAppName("Spark Example")
        .set("spark.task.maxFailures", "6")

    val sparkContext = JavaSparkContext(sparkConf)

    val items = listOf("123/643/7563/2134/ALPHA", "2343/6356/BETA/2342/12", "23423/656/343")

    val distributedDataset = sparkContext.parallelize(items)


    val sumOfNumbers = distributedDataset.flatMap { it.split("/").iterator() }
        .filter { it.matches(Regex("[0-9]+")) }
        .map { it.toInt() }
        .reduce { accumulator, element -> accumulator + element  }

    println("########## Suma Numerelor: ${sumOfNumbers} ############")

    val lines = sparkContext.textFile("src/main/resources/data.txt")

    lines.persist(StorageLevel.MEMORY_ONLY())

    val totalLength0 = lines.map { s->s.length }.reduce { acc, t -> acc + t }

    val totalLength1= lines.map(object : Function<String?, Int?> {
        override fun call(p0: String?): Int? {
            return p0?.length ?: 0
        }
    }).reduce(object : Function2<Int?, Int?, Int?> {
        override fun call(p0: Int?, p1: Int?): Int? {
            return (p0?.toInt() ?: 0) + (p1?.toInt() ?: 0)
        }
    })

    val totalLength2 = lines.map(GetLength()).reduce(Sum())


    println("##### Rezultate Linii Text #####")
    println("Metoda 0 (Lambda): $totalLength0")
    println("Metoda 1 (Inline): $totalLength1")
    println("Metoda 2 (Clase): $totalLength2")
    println("################################")


    val broadcast: Broadcast<List<Int>> = sparkContext.broadcast(listOf<Int>(1,2,3))
    val totalLength3 = lines.map { s -> s.length + broadcast.value()[0]}.reduce { acc, t -> acc + t }
    println("###### Rezultat Broadcast: $totalLength3 ######")

    val accumulator = sparkContext.sc().longAccumulator()
    sparkContext.parallelize(listOf(1,2,3,4)).foreach { x->accumulator.add(x.toLong()) }


    println("######## Valoare Acumulator: ${accumulator.value()} ########")

    sparkContext.stop()

}