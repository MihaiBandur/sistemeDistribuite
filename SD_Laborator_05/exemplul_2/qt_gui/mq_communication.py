import pika
import threading
from PyQt6.QtCore import QMetaObject, Qt, Q_ARG


class RabbitMq(threading.Thread):
    config = {
        'host': 'localhost',
        'port': 5672,
        'username': 'guest',
        'password': 'guest',
        'exchange': 'libraryapp.direct',
        'routing_key': 'libraryapp.routingkey1',
        'queue': 'libraryapp.queue'
    }

    def __init__(self, ui):
        super().__init__()
        self.ui = ui
        credentials = pika.PlainCredentials(self.config['username'], self.config['password'])
        self.parameters = pika.ConnectionParameters(
            host=self.config['host'],
            port=self.config['port'],
            credentials=credentials
        )

        self.connection = None
        self.channel = None

        self.daemon = True

        self.start()

    def run(self):
        self.connection = pika.SelectConnection(
            parameters=self.parameters,
            on_open_callback=self.on_connection_open,
            on_open_error_callback=self.on_connection_open_error,
            on_close_callback=self.on_connection_closed
        )

        try:
            self.connection.ioloop.start()
        except Exception as e:
            print(f"Bucla asincrona s-a oprit: {e}")

    def on_connection_open(self, connection):
        print("Conexiune asincrona deschisa cu succes.")
        connection.channel(on_open_callback=self.on_channel_open)

    def on_connection_open_error(self, connection, err):
        print(f"Eroare la conectare: {err}")

    def on_connection_closed(self, connection, reason):
        print(f"Conexiune închisă: {reason}")
        self.connection.ioloop.stop()

    def on_channel_open(self, channel):
        self.channel = channel
        self.channel.queue_declare(
            queue=self.config['queue'],
            durable=True,
            callback=self.on_queue_declared
        )

    def on_queue_declared(self, frame):
        self.channel.basic_consume(
            queue=self.config['queue'],
            on_message_callback=self.on_received_message,
            auto_ack=False
        )

    def on_received_message(self, channel, deliver, properties, message):
        result = message.decode('utf-8')

        channel.basic_ack(delivery_tag=deliver.delivery_tag)

        try:
            # Thread-safe UI update
            QMetaObject.invokeMethod(
                self.ui,
                "set_response",
                Qt.ConnectionType.QueuedConnection,
                Q_ARG(str, result)
            )
        except Exception as e:
            print(f"Eroare la setarea raspunsului în UI: {e}")

    def send_message(self, message):
        if self.connection and self.connection.is_open:
            cb = lambda: self._publish_internal(message)
            self.connection.ioloop.add_callback_threadsafe(cb)
        else:
            print("Conexiunea nu este încă gata")

    def _publish_internal(self, message):
        if self.channel and self.channel.is_open:
            self.channel.basic_publish(
                exchange=self.config['exchange'],
                routing_key=self.config['routing_key'],
                body=message
            )