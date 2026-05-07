package com.sd.laborator

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CopyOnWriteArrayList

@Component
class KafkaMonitor {
    companion object{
        private  var numberOfBids: Int = 0;
        private  var numberOfProcessedBids: Int = 0;
        private var BASE_DOCKER_API_COMMAND: String = "curl --unix-socket /var/run/docker.sock http:/v1.40"
    }

    val timeHistory = CopyOnWriteArrayList<String>()
    val bidsHistory = CopyOnWriteArrayList<Int>()
    val processedBidsHistory = CopyOnWriteArrayList<Int>()
    init {
        println("KafkaMonitor instantied")
    }

    @KafkaListener(
        groupId = "KafkaMonitor",
        topicPartitions = [
            TopicPartition(
                topic = "topic_oferte",
                partitionOffsets = [
                    PartitionOffset(partition = "0", initialOffset = "0"),
                    PartitionOffset(partition = "1", initialOffset = "0"),
                    PartitionOffset(partition = "2", initialOffset = "0"),
                    PartitionOffset(partition = "3", initialOffset = "0")
                ]
            ),
            TopicPartition(
                topic = "topic_oferte_procesate",
                partitionOffsets = [
                    PartitionOffset(partition = "0", initialOffset = "0")
                ]
            )
        ]
    )
    fun  monitorKafkaMessage(message: ConsumerRecord<String, String>){
        when(message.topic()){
            "topic_oferte" -> ++numberOfBids
            "topic_oferte_procesate" -> ++numberOfProcessedBids
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun showKafkaStats(){
        val  now = LocalDateTime.now()
        println("[$now] Grad incarcare Auctioneer: $numberOfBids oferte primite")
        println("[$now] Grad incarcare MessageProcessor: $numberOfProcessedBids oferte procesate")

        val timeString = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        timeHistory.add(timeString)
        bidsHistory.add(numberOfBids)
        processedBidsHistory.add((numberOfBids))

        if (timeHistory.size > 60) {
            timeHistory.removeAt(0)
            bidsHistory.removeAt(0)
            processedBidsHistory.removeAt(0)
        }
    }


    @Scheduled(fixedDelay = 2000)
    fun showAuctioneerContainersStats() {
        val auctioneerContainersFilter =
            URLEncoder.encode("{\"ancestor\": [\"auctioneer_docker_auctioneer:latest\"]}", "utf-8")
        val auctioneerListProcess: Process =
            Runtime.getRuntime().exec("$BASE_DOCKER_API_COMMAND/containers/json?filters=$auctioneerContainersFilter")
        val auctioneerListProcessInput = BufferedReader(InputStreamReader(auctioneerListProcess.inputStream))

        val auctioneerListOutput = auctioneerListProcessInput.readLine()
        auctioneerListProcessInput.close()

        if(auctioneerListOutput == null){
            return
        }

        val containerIdRegex = Regex("\"Id\":\"([a-f0-9]*)\"")
        containerIdRegex.findAll(auctioneerListOutput).forEach {
            val auctioneerContainerID = it.groupValues[1].take(12)

            val auctioneerContainerStatsProcess: Process = Runtime.getRuntime().exec("$BASE_DOCKER_API_COMMAND/containers/$auctioneerContainerID/stats?stream=0")
            val auctioneerContainerStatsProcessInput = BufferedReader(InputStreamReader(auctioneerContainerStatsProcess.inputStream))

            val auctioneerContainerStatsOutput = auctioneerContainerStatsProcessInput.readLine()

            if(auctioneerContainerStatsOutput != null) {
                val cpuUsageRegex = Regex("\"cpu_stats\":\\{\"cpu_usage\":\\{\"total_usage\":([0-9]*),")
                cpuUsageRegex.find(auctioneerContainerStatsOutput)?.groupValues?.get(1)?.let { cpu ->
                    println("[$auctioneerContainerID] Utilizare procesor: $cpu")
                }

                val memoryUsageRegex = Regex("\"memory_stats\":\\{\"usage\":([0-9]*),")
                memoryUsageRegex.find(auctioneerContainerStatsOutput)?.groupValues?.get(1)?.let { mem ->
                    println("[$auctioneerContainerID] Utilizare memorie: $mem")
                }
            }

        }
    }
}

@RestController
class MetricsController(private val kafkaMonitor: KafkaMonitor){
    @GetMapping("/api/metrics")
    fun genMetrics(): Map<String, Any>{
        return mapOf(
            "time" to kafkaMonitor.timeHistory,
            "bids" to kafkaMonitor.bidsHistory,
            "processed" to kafkaMonitor.processedBidsHistory
        )
    }
}