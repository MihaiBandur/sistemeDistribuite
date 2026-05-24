import time
import json
import random
import threading
from pynput.mouse import Controller
from kafka import KafkaProducer

mouse = Controller()

try:
    producer = KafkaProducer(
        bootstrap_servers = ['localhost:9092'],
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )
except Exception as e:
    print(f'eroare conectare la kafka: {e}')
    exit(1)


DURATA_RULARE = 10 
INTERVAL_ESANTIONARE = 0.1 

def producer_mouse(durata):
    time_start = time.time()
    while time.time() - time_start < durata:
        x, y = mouse.position
        print(f'Pozitia mouse-ului {x} si {y}')
        mesaj = {
            "tip": "mouse",
            "x": x,
            "y": y,
            "timestamp": time.time()
        }

        producer.send('mouse-topic', value=mesaj)
        time.sleep(INTERVAL_ESANTIONARE)


def producer_aleator(durata):
    timp_start = time.time()

    while time.time() - timp_start < durata:
        rand_x = random.randint(0, 1920)
        rand_y = random.randint(0, 1080)
        print(f'Pozitii aleatorii {rand_x} si {rand_y}')
        mesaj = {
            "tip": "aleator",
            "rand_x": rand_x,
            "rand_y": rand_y,
            "timestamp": time.time()
        }
        
        # Trimitem către topicul 'random-topic'
        producer.send('random-topic', value=mesaj)
        time.sleep(INTERVAL_ESANTIONARE)

if __name__ == "__main__":
    print(f"Începem testul de {DURATA_RULARE} secunde...")
    
    
    thread_mouse = threading.Thread(target=producer_mouse, args=(DURATA_RULARE,))
    thread_aleator = threading.Thread(target=producer_aleator, args=(DURATA_RULARE,))
    
    
    thread_mouse.start()
    thread_aleator.start()
    
    
    thread_mouse.join()
    thread_aleator.join()
    
    
    producer.flush()
    producer.close()
