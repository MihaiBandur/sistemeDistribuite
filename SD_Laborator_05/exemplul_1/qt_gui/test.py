import pika
import pika.credentials

creds = pika.PlainCredentials(username="student", password="student")
# Define connection parameters
connection_params = pika.ConnectionParameters(host='192.168.23.129', credentials=creds, port=5672)

# Establish connection
connection = pika.BlockingConnection(connection_params)
channel = connection.channel()

# Declare an exchange (if not already declared)
exchange_name = "stackapp.direct"
# channel.exchange_declare(exchange=exchange_name, exchange_type="direct")

# Publish a message
routing_key = "stackapp.routingkey"  # The queue must be bound to this key
message = "Hello, RabbitMQ!"
channel.basic_publish(exchange=exchange_name, routing_key=routing_key, body=message)

print(f"Sent: '{message}' to exchange '{exchange_name}'")

# Close connection
connection.close()
