from pyexpat.errors import messages

import  pika
import json
import threading

from retry import  retry

class RabbitMqListener:
    config = {
        'host': 'localhost',
        'port': 5672,
        'username': 'guest',
        'password': 'guest',
        'exchange': 'restaurant.direct',
        'orders_routing_key': 'restaurant.orders.key',
        'orders_queue': 'restaurant.orders.queue',
        'responses_routing_key': 'restaurant.responses.key',
        'responses_queue': 'restaurant.responses.queue',
    }

    def __init__(self, on_response_callback):
        self.on_response_callback = on_response_callback
        credentials = pika.PlainCredentials(
            self.config['username'], self.config['password']
        )
        self.parameters =  pika.ConnectionParameters(
            host=self.config['host'],
            port=self.config['port'],
            credentials=credentials
        )

        self._listener_thread = threading.Thread(
            target=self._start_listening, daemon=True
        )

        self._listener_thread.start()

    def send_order(self, order: dict):



        message = json.dumps(order).encode('utf-8')
        try:
            with pika.BlockingConnection(self.parameters) as conn:
                with conn.channel() as ch:
                    ch.basic_publish(
                        exchange=self.config['exchange'],
                        routing_key=self.config['orders_routing_key'],
                        body=message,
                        properties=pika.BasicProperties(
                            delivery_mode=2,
                            content_type='application/json'  # <-- ADAUGI ASTA
                        )
                    )
        except Exception as e:
            print(f"[MQ] Eroare: {e}")


    @retry(pika.exceptions.AMQPConnectionError, delay=5, jitter=(1, 3))
    def _start_listening(self):
        try:
            with pika.BlockingConnection(self.parameters) as conn:
                with conn.channel() as ch:
                    ch.basic_consume(
                        queue=self.config['responses_queue'],
                        on_message_callback=self._on_response,
                        auto_ack=True
                    )
                    print("RabbitMQ ~> Ascult raspunsuri de la bucatari...")
                    ch.start_consuming()
        except pika.exceptions.ConnectionClosedByBroker:
            print("RabbitMQ ~> Conexiunea inchisa de broker.")
        except pika.exceptions.AMQPChannelError as e:
            print(f"RabbitMQ ~> Eroare canal: {e}")
        except KeyboardInterrupt:
            print("RabbitMQ ~> Oprit.")

    def _on_response(self, channel, method, properties, body):
        try: 
            decoded = body.decode('utf-8').strip('"').replace('\\"', '"')
            response = json.loads(decoded)
            print(f"RabbitMQ ~> Raspuns primit: {response}")
            if self.on_response_callback:
                self.on_response_callback(response)
        except Exception as e:
            print(f"RabbitMQ ~> Eroare: {e}")