#!/bin/zsh

echo "Pornim 100 de bidders"
for i in {1..100}
do

  java -jar  out/artifacts/BidderMicroservice_jar/BidderMicroservice.jar > /dev/null 2>&1 &

done

echo "S-au pornit 100 de bidders fara probleme"