#!/bin/bash

echo "=== Pornire Sistem Licitatii ==="

# Definim directorul unde IntelliJ a compilat artefactele JAR
BASE_DIR="out/artifacts"

# 1. Pornim serviciile de infrastructura
echo "[1/5] Pornire MasterLoggerMicroservice..."
java -jar $BASE_DIR/MasterLoggerMicroservice_jar/MasterLoggerMicroservice.jar > master_logger.log 2>&1 &
sleep 2

echo "[2/5] Pornire HeartbeatMicroservice..."
java -jar $BASE_DIR/HeartbeatMicroservice_jar/HeartbeatMicroservice.jar > heartbeat.log 2>&1 &
sleep 2

# 2. Pornim procesoarele de mesaje
echo "[3/5] Pornire MessageProcessor si BiddingProcessor..."
java -jar $BASE_DIR/MessageProcessorMicroservice_jar/MessageProcessorMicroservice.jar > message_processor.log 2>&1 &
java -jar $BASE_DIR/BiddingProcessorMicroservice_jar/BiddingProcessorMicroservice.jar > bidding_processor.log 2>&1 &
sleep 2

# 3. Pornim nodul central de licitatie (care declanseaza timer-ul de 15 secunde)
echo "[4/5] Pornire AuctioneerMicroservice..."
java -jar $BASE_DIR/AuctioneerMicroservice_jar/AuctioneerMicroservice.jar > auctioneer.log 2>&1 &
sleep 1 # o scurta pauza pentru a lasa portul 1500 sa se deschida

# 4. Pornim ofertantii (Bidderii)
NUM_BIDDERS=5
echo "[5/5] Pornire a $NUM_BIDDERS instante de BidderMicroservice..."
for i in $(seq 1 $NUM_BIDDERS)
do
    java -jar $BASE_DIR/BidderMicroservice_jar/BidderMicroservice.jar > bidder_$i.log 2>&1 &
done

echo "======================================================"
echo "Toate microserviciile ruleaza in fundal cu succes!"
echo ""
echo "Pentru a vizualiza log-ul central in timp real, ruleaza:"
echo "tail -f MASTER_LOG_CENTRAL.txt"
echo ""
echo "Cand licitatia s-a terminat, opreste toate procesele Java ruland:"
echo "killall java"
echo "======================================================"