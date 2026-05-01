# SD Laborator 09

## Docker compose pentru rabbit mq

Pentru simplitate puteti folosi fisierul de `docker-rabbit/docker-compose.yml`.

```bash
cd docker-rabbit
docker compose up -d
```

## URI-ul fisierului jar pe sistem Windows

Pe Windows uri-ul va arata cam asa `file:///D:/SD/SD_Laborator_09/exemplul%201/DataFlow%20Source/target/DataFlowSource-1.0-SNAPSHOT.jar`. Observati un `/` in minus la inceput si folosirea `/` in loc de `\` cum e deobicei pe Windows.


## RabbitMQ hostat altundeva decat pe localhost

Daca folositi o instanta de RabbitMQ care nu este pe localhost ci intr-un alt VM sau chiar in cloud, atunci la deploy-ul stream-ului va trebui sa adaugati urmatoarea optiune:

```dataflow
stream deploy --name time-to-log --properties "app.*.spring.rabbitmq.addresses=amqp://myuser:mypass@my-rabbitmq.cloud.com:5672"
```

Acestea sunt doar informatii aditionale celor prezente deja in laboratorul de pe site.

Spor!