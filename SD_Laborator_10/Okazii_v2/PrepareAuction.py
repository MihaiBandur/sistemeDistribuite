import os
import time
from kafka import KafkaAdminClient, KafkaConsumer
from kafka.admin import NewTopic
from kafka.errors import TopicAlreadyExistsError  # <-- Am importat eroarea specifica

KAFKA_SERVER = os.environ.get("KAFKA_SERVER", "localhost:9092")

if __name__ == '__main__':
    admin = KafkaAdminClient(bootstrap_servers=KAFKA_SERVER)

    used_topics = (
        "topic_oferte",
        "topic_rezultat",
        "topic_oferte_procesate",
        "topic_notificare_procesor_mesaje",
    )

    print("Se sterg topic-urile existente...")

    # Preluam lista de topic-uri curente
    kafka_topics = KafkaConsumer(bootstrap_servers=KAFKA_SERVER).topics()

    # Identificam exact ce trebuie sters
    topics_to_delete = [topic for topic in used_topics if topic in kafka_topics]

    if topics_to_delete:
        for topic in topics_to_delete:
            print("\tSe sterge {}...".format(topic))

        try:
            # Trimitem cererea de stergere pentru toate odata
            admin.delete_topics(topics=topics_to_delete, timeout_ms=5000)
        except Exception:
            pass

        print("\tSe asteapta finalizarea stergerii asincrone (8 secunde)...")
        time.sleep(8)  # Am marit putin pauza pentru siguranta
    else:
        print("\tNu exista topic-uri de sters din rulari anterioare.")

    print("Se creeaza topic-urile necesare:")
    lista_topicuri = [
        NewTopic(name=used_topics[0], num_partitions=4, replication_factor=1),
        NewTopic(name=used_topics[1], num_partitions=1, replication_factor=1),
        NewTopic(name=used_topics[2], num_partitions=1, replication_factor=1),
        NewTopic(name=used_topics[3], num_partitions=1, replication_factor=1)
    ]

    for topic in lista_topicuri:
        print("\t{}".format(topic.name))

    # Aici este magia: prindem eroarea daca topic-ul inca exista
    try:
        admin.create_topics(lista_topicuri, timeout_ms=10000)
        print("Topic-urile au fost create cu succes!")
    except TopicAlreadyExistsError:
        print(
            "Avertisment: Anumite topic-uri inca exista in Kafka (probabil stergerea e in desfasurare). Scriptul continua fara probleme.")
    except Exception as e:
        print("A aparut o alta eroare la creare: {}".format(e))

    print("Gata! Microserviciile participante la licitatie pot fi pornite.")