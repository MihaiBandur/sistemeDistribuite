# Kafka in docker

In aceasta configuratie nu mai este nevoie de nimic in plus in afara de a porni containerele folosind comanda:

```bash
docker-compose up -d
```

Daca doriti ca serverul sa poata fi accesat si din alte locatii in afara de localhost atunci va trebui sa setati variabila de mediu `KAFKA_HOST` inainte de a porni containerele.

De exemplu:

```bash
KAFKA_HOST=192.168.23.129 docker compose up -d
```

Ulterior pornirii putem intra in container cu urmatoarea comanda si avea acess la utilitarele de kafka precum `kafka-topics` etc.

```bash
docker exec -it lab10kafka bash
```

## Creare topic
```bash
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
```

## Verificare topic
```bash
kafka-topics --bootstrap-server localhost:9092 --describe --topic test
```

## Producere mesaje intr-un topic (asteapta linie cu linie cate un mesaj de la tastatura)
```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic test
```

## Consumare mesaje dintr-un topic
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
```
