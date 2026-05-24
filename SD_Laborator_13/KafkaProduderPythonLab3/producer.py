from kafka import KafkaProducer
import re
import time
import json


producer = KafkaProducer(
    bootstrap_servers =  ['localhost:9092'],
    value_serializer = lambda x: x.encode('utf-8')
)

NUME_TOPIC = 'cuvinte-topic'
my_file = '5000-8.txt'


try:
    with open(my_file, 'r', encoding='utf-8') as f:
        for line in f:
            cuvinte = re.findall(r'\b[a-zA-Z]+\b', line.lower())

            for cuvant in cuvinte:
                    producer.send(NUME_TOPIC, cuvant)

                    time.sleep(0.01)

    producer.flush()

except FileNotFoundError:
    print(f"Eroare: Nu am putut gasi fisierul {my_file}")
except Exception as e:
    print(f"A aparut o eroare la conexiunea cu Kafka: {e}")
finally:
     producer.close()

