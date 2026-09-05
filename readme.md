# Teme Sisteme Distribuite

## Lab 1
* **Application Architecture**: Developed a Java-based web application following a client-server model.
* **Packaging**: Bundled the client-side interfaces and web components into a single WAR (Web ARchive) file for streamlined deployment.
* **Server Deployment**: Deployed and hosted the WAR application on a GlassFish application server.
* **Data Persistence**: Integrated a MySQL database to securely store, manage, and retrieve the required application information.

## Lab 2

* **Enterprise Packaging**: Developed the business logic tier and packaged the complete application as an EAR (Enterprise ARchive) file for enterprise-level deployment.
* **Database Integration**: Transitioned data persistence to an SQLite database for lightweight, file-based storage.
* **Concurrency & Monitoring**: Implemented a dedicated background thread to continuously monitor specific numeric parameters, ensuring they remain within a safe range.
* **Alerting System**: Integrated an alerting mechanism to proactively notify the user if the monitored parameters exceed the defined thresholds.
* **EJB Implementation**: Gained practical knowledge of Enterprise JavaBeans (EJB) by implementing and differentiating between **Stateless** and **Stateful** session beans to manage application state and business processes.

## Lab 3

* **Geographic Access Control**: Implemented a location-based security filter that reads the host operating system's locale to block service access for specific blacklisted geographic zones.
* **Service Chaining Architecture**: Designed and implemented a pipeline-style distributed pattern where each service is tightly coupled to the next, sequentially passing data down the chain (Filtering -> Geocoding -> Weather).
* **Service Orchestration Architecture**: Reprojected the application using a centralized "Orchestrator" (Dirijor) pattern on a separate Git branch. This completely decoupled the underlying services, allowing the Orchestrator to independently manage the workflow and decision-making process.

## Lab 4

* **Stack**: Kotlin + Spring Boot (Maven), Python + Flask, Postman.
* **REST API (PhoneAgenda)**: Built a Kotlin/Spring Boot REST service exposing a full CRUD contract over a phone agenda (`POST`, `GET`, `PUT`, `PATCH`, `DELETE` on `/person`), returning the correct HTTP status codes for each operation.
* **JSON Patch**: Implemented partial updates using RFC 6902 `JsonPatch` operations applied over the resource with Jackson's `ObjectMapper`.
* **Layered Architecture**: Structured the `PasswordEncryptionExample` app into `presentation` / `business` / `persistence` packages with interfaces and dependency injection for every service.
* **Security & Encryption**: Implemented user registration/login with Spring Security password encoders (hashing + matching) and AES encryption of sensitive data, with the secret key loaded from a PKCS12 keystore.
* **Python Front-End**: Wrote a Flask web interface that consumes the Kotlin REST API with `requests` (register, login, session handling and expense listing).
* **API Testing**: Exported a Postman collection to test all endpoints of the services.

## Lab 5

* **Stack**: Kotlin + Spring AMQP, Python (PyQt6 / tkinter + pika), RabbitMQ, Docker Compose.
* **Message Broker**: Ran RabbitMQ (`rabbitmq:3-management`) in Docker Compose and configured exchanges, queues and bindings from the management UI on port 15672.
* **Asynchronous Communication**: Connected Python GUIs to Kotlin back-ends purely through RabbitMQ messages — the GUI publishes commands on an exchange and listens for the answers on a response queue (`pika` on the Python side, `@RabbitListener` / `RabbitTemplate` on the Kotlin side).
* **StackApp (example 1)**: Kotlin service that generates stacks of prime numbers and computes the union and the cartesian product of two sets, splitting the work into chained step-services; driven from both a PyQt and a tkinter interface.
* **LibraryApp (example 2)**: Book library with a DAO layer and interchangeable printers (raw / JSON / XML / HTML), controlled from a PyQt6 GUI that can also add new books.
* **Homework — "Bucataria" (Restaurant)**: Modelled a restaurant where a PyQt6 waiter GUI publishes orders on a queue and several Kotlin "cook" instances consume them concurrently. Each cook generates its own unique id, rejects an order already taken by someone else, simulates a random preparation time and publishes back the order status (`IN_PROGRESS` / `DONE` / `REJECTED`).

## Lab 6

* **Stack**: Kotlin + Spring Boot + Spring JDBC, SQLite, RabbitMQ, Python (Flask + pika).
* **Persistence Layer**: Added a real database tier over the previous applications using `JdbcTemplate` and custom `RowMapper` implementations on top of an SQLite file database, with the repositories creating their own tables at start-up.
* **LibraryApp**: Refactored the library into `business` / `persistance` / `presentation` layers, exposing both REST endpoints (`/print?format=html|json|raw`, `/find`, `/add`) and a RabbitMQ entry point, with a Flask web GUI as client.
* **BeerApp**: CRUD application over a `beers` table (add, list, search by name, search by price, update, delete) driven entirely through RabbitMQ from a Python CLI client.
* **Configuration**: Externalised the broker and datasource settings in `application.properties` so the same jar can point to any RabbitMQ instance.

## Lab 7

* **Stack**: Kotlin, Java sockets (TCP + UDP), RxJava 3, coroutines, Bash.
* **Okazii — Auction System**: Implemented a distributed auction made of independent microservices communicating over raw TCP sockets: `Auctioneer` (collects bids for 15 seconds), `Bidder` (multiple instances), `BiddingProcessor` (decides the winner) and `MessageProcessor` (routes messages).
* **Custom Protocol**: Wrote a shared `MessageLibrary` with an application-level message format (timestamp, sender ip/port, identity, body) and its own `serialize` / `deserialize` routines.
* **Reactive Streams**: Used RxJava `Observable` + `CompositeDisposable` to handle incoming connections and bids asynchronously.
* **Fault Tolerance**: Added an `ExecutionJournal` library that persists every operation to a journal file, so a service that crashes can detect unfinished operations at start-up and resume them (recovery).
* **Heartbeat & Monitoring**: Built a `HeartbeatMicroservice` that receives UDP heartbeats into a service registry and automatically restarts the jars of the services that stop reporting, plus a `MasterLoggerMicroservice` that centralises all UDP log messages into a single log file.
* **Automation**: Bash scripts (`run_okazii.sh`, `start_bidders.sh`) that start the whole system and scale the number of bidders.

## Lab 8

* **Stack**: Kotlin, Docker, Docker Compose, Maven & Gradle, Python (PyQt6), SQLite.
* **Containerisation**: Wrote `Dockerfile`s for every microservice (`amazoncorretto` base image + fat jar built with the Maven/Gradle assembly plugins) and compared building the same Hello microservice with both Maven and Gradle.
* **Multi-Container Orchestration**: Composed the whole "teacher–student" system with `docker-compose`: `MessageManager`, `Teacher`, `Student`, `Database` and `Heartbeat` services running on a dedicated bridge network.
* **Container Configuration**: Replaced hard-coded hosts with environment variables (`MESSAGE_MANAGER_HOST`, `DB_HOST`, `HEARTBEAT_INTERVAL_MS`) so the services find each other by container name, and enabled `stdin_open`/`tty` for the interactive service.
* **Volumes**: Persisted the SQLite database outside the container through a named Docker volume.
* **Application Logic**: The Student microservices answer questions from their own question database, the Teacher broadcasts questions through the MessageManager, and a PyQt6 UI is used by the teacher to ask questions and see the answers.

## Lab 9

* **Stack**: Kotlin + Spring Cloud Stream / Spring Cloud Data Flow, RabbitMQ (Docker), Python + Flask.
* **Stream Processing (example 1)**: Built a classic `Source → Processor → Sink` pipeline (a time source polling every 10 seconds, a processor formatting the timestamp, a log sink), registered the jars in Spring Cloud Data Flow and deployed the stream over RabbitMQ.
* **Homework — Supply Chain Pipeline (example 2)**: Modelled a medical-supplies ordering flow with independent microservices bound to the stream: `Client`, `Comanda` (order intake), `Depozit` (warehouse), `Facturare` (invoicing), `Livrare` (delivery), `Reaprovizionare` (restocking) and a `Database` microservice.
* **Binding & Transformation**: Used `@EnableBinding(Processor)` and `@Transformer(inputChannel, outputChannel)` so each service consumes the message from the previous stage and emits its result to the next one.
* **Mixed Communication**: Combined stream messaging with synchronous REST calls (`RestTemplate`) towards the Database microservice for persisting clients and orders.
* **Web GUI**: Flask interface for placing an order (product, quantity, delivery address) that is injected into the stream through the Client microservice.

## Lab 10

* **Stack**: Apache Kafka + Zookeeper (Docker Compose), Python (`kafka-python`), Kotlin + Spring Kafka, Docker.
* **Kafka Setup**: Started a full Kafka cluster in Docker and worked with `kafka-topics` inside the container to create topics, set the number of partitions and inspect consumer groups.
* **Producer / Consumer Basics**: Python examples with `KafkaProducer` / `KafkaConsumer` on separate threads, and a Spring Boot (Kotlin) application with a `Producer`, a `Consumer` and a REST controller for publishing messages.
* **Okazii v2 — Auction on Kafka**: Rewrote the lab 7 auction on top of Kafka in Python: bidders publish their offer as a message with headers (identity + amount) on a bids topic, the auctioneer and the bidding processor consume from partitioned topics, and duplicated bids are generated on purpose to practice deduplication.
* **Scaling with Docker**: Packaged `Auctioneer` and `Bidder` as `python:alpine` images with their own compose files so multiple bidder instances can be scaled up easily.
* **Monitoring**: Kotlin monitoring application using `@KafkaListener` with explicit `TopicPartition` / `PartitionOffset` to replay both topics from offset 0, counting the received vs. processed bids, and querying the Docker daemon through `/var/run/docker.sock` to report the state of the containers.

## Lab 11

* **Stack**: Kotlin + Micronaut (serverless functions), RabbitMQ, MySQL, Docker Compose, Maven.
* **Serverless / FaaS**: Implemented Micronaut functions (`@FunctionBean` over `Function` / `Supplier`) that can be invoked as standalone serverless units, with `logback` logging and injected singleton services.
* **Sieve of Eratosthenes**: Function that computes all the primes up to a given limit and filters the received list of numbers, returning only the prime ones together with a summary message.
* **Function Chaining**: A `file_reader` supplier function reads the numbers from a resource file and calls the Eratosthenes function through a declarative Micronaut HTTP client, so one function triggers another.
* **Recursive Sequence**: Function computing the n-th term of a recursive sequence, with validation of the maximum accepted input.
* **Counter App (problem 2)**: Event-driven click counter — a REST controller publishes `ClickEvent`s to RabbitMQ, a `@RabbitListener` consumer increments the per-button counter and persists it in MySQL through a Micronaut Data repository; both RabbitMQ and MySQL are started with Docker Compose.
* **Feed Producer/Consumer (problem 4)**: A producer function that downloads an RSS feed and POSTs it to a consumer function which parses and processes it.

## Lab 12

* **Stack**: Hadoop (HDFS + YARN + MapReduce), Hadoop Streaming, Python 3, Bash, SQLite.
* **Cluster Installation**: Wrote helper Bash scripts to install Java 8 and Hadoop and to copy the configuration files (`core-site.xml`, `hdfs-site.xml`, `mapred-site.xml`, `yarn-site.xml`), plus the `sshd_config` needed for the passwordless SSH between nodes.
* **MapReduce with Hadoop Streaming**: Implemented several mapper/reducer pairs in Python, reading from `stdin` and writing `key\tvalue` to `stdout`, submitted to the cluster through the Hadoop Streaming jar.
* **Word Count & Letter Histogram**: Classic word count plus a job counting how many words start with each letter of the alphabet.
* **Distributed Grep**: Mapper that executes commands received as input and filters their output against a regex passed as a job argument.
* **Web Crawling Jobs**: A sitemap job that downloads pages and extracts all the `href` links, and a "top words" job that computes the top 5 words per page and the global ranking, ignoring stopwords.
* **Browser History Analysis**: Python script that extracts the Safari `History.db` with `sqlite3`, then a MapReduce job that aggregates the number of visits per host.

## Lab 13

* **Stack**: Apache Spark (RDD, Spark SQL, Spark Streaming) in Kotlin, Apache Kafka, MySQL, Docker Compose, Python (`kafka-python`, `pynput`).
* **Infrastructure**: Docker Compose configuration starting Kafka + Zookeeper and a MySQL instance initialised from an SQL script, used as data sources for the Spark jobs.
* **RDD API**: Transformations and actions on RDDs (`map`, `reduce`, `flatMap`, `mapToPair`, `reduceByKey`, `sortByKey`) together with broadcast variables and persistence levels.
* **Spark SQL**: Built `Dataset`/`DataFrame`s from JSON files and from a MySQL table over JDBC, ran aggregations with the SQL API and wrote the results back as JSON.
* **Character Histogram — three ways**: Solved the same problem with the RDD API, with Spark SQL (`explode` / `split` / `groupBy`) and with Spark Streaming reading new files from a monitored directory, to compare the three programming models.
* **Spark + Kafka Streaming**: Direct streams created with `KafkaUtils.createDirectStream` consuming multiple topics, with a stateful word ranking (`updateStateByKey` + checkpointing).
* **Homework**: (1) a job consuming two Kafka topics at the same time — mouse coordinates and random numbers produced by a Python producer using `pynput` and JSON serialisation; (2) a stateful job computing the running mean and the dispersion (variance) of the received values, using a custom serialisable state class and checkpoints; (3) a job reading from a TCP socket stream and filtering out a list of forbidden words.
