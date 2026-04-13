# Java-Kafka – Zero to Hero

A hands-on Java project that walks you through **Apache Kafka** from the very basics all the way to Kafka Streams.  Every concept is backed by working, tested code.

---

## Table of Contents

1. [What is Kafka?](#1-what-is-kafka)
2. [Core Concepts](#2-core-concepts)
3. [Prerequisites](#3-prerequisites)
4. [Project Structure](#4-project-structure)
5. [Quick Start – Running Kafka Locally](#5-quick-start--running-kafka-locally)
6. [Examples](#6-examples)
   - [Producer](#61-producer)
   - [Consumer](#62-consumer)
   - [AdminClient](#63-adminclient)
   - [Kafka Streams – Word Count](#64-kafka-streams--word-count)
7. [Running the Tests](#7-running-the-tests)
8. [Building the Project](#8-building-the-project)
9. [Key Configuration Properties](#9-key-configuration-properties)
10. [Further Reading](#10-further-reading)

---

## 1. What is Kafka?

**Apache Kafka** is an open-source, distributed event-streaming platform capable of handling trillions of events a day.  It is used for:

| Use-case | Description |
|---|---|
| **Messaging** | Decouple services with durable, ordered queues |
| **Activity tracking** | Capture user events and audit logs at scale |
| **Stream processing** | Transform and aggregate data in real time |
| **Event sourcing** | Replay history to rebuild application state |
| **Log aggregation** | Centralise logs from many services |

---

## 2. Core Concepts

```
Producer ──► Topic (partitioned & replicated) ──► Consumer Group
                │
                └──► Kafka Streams ──► Output Topic
```

| Concept | Description |
|---|---|
| **Broker** | A single Kafka server that stores and serves data |
| **Topic** | A named, ordered log of records |
| **Partition** | A topic is split into partitions for parallelism; each partition is an ordered, immutable log |
| **Offset** | The position of a record within a partition (starts at 0) |
| **Producer** | Writes records to a topic |
| **Consumer** | Reads records from a topic, tracking its position via offsets |
| **Consumer Group** | Multiple consumers sharing the work of reading a topic; each partition is assigned to exactly one member |
| **Broker replication** | Each partition has one **leader** and zero or more **followers** (ISR) |
| **Kafka Streams** | A client library for building stateful/stateless stream-processing apps on top of Kafka |

---

## 3. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java | 17+ | Tested with OpenJDK 17 |
| Maven | 3.8+ | Build & dependency management |
| Docker | 24+ | Easiest way to run a local broker |

---

## 4. Project Structure

```
Java-Kafka/
├── pom.xml                                          # Maven build descriptor
└── src/
    ├── main/
    │   ├── java/com/kafka/
    │   │   ├── producer/ProducerDemo.java           # Kafka Producer API
    │   │   ├── consumer/ConsumerDemo.java           # Kafka Consumer API
    │   │   ├── admin/AdminClientDemo.java           # AdminClient API
    │   │   └── streams/WordCountApp.java            # Kafka Streams DSL
    │   └── resources/logback.xml                    # Logging configuration
    └── test/
        └── java/com/kafka/
            ├── producer/ProducerDemoTest.java       # MockProducer tests
            ├── consumer/ConsumerDemoTest.java       # MockConsumer tests
            └── streams/WordCountAppTest.java        # TopologyTestDriver tests
```

---

## 5. Quick Start – Running Kafka Locally

The simplest way to get a single-node Kafka broker running is with Docker:

```bash
# Apache Kafka 3.7 in KRaft mode (no ZooKeeper needed)
docker run -d \
  --name kafka \
  -p 9092:9092 \
  apache/kafka:3.9.2
```

Verify it is up:

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

Create the demo topic (optional – the code handles it gracefully when auto-creation is enabled):

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic demo-topic \
  --partitions 3 --replication-factor 1
```

---

## 6. Examples

### 6.1 Producer

**File:** `src/main/java/com/kafka/producer/ProducerDemo.java`

Key concepts demonstrated:

* Creating a `KafkaProducer` with `StringSerializer`
* **Idempotent producer** (`enable.idempotence=true`) – guarantees exactly-once delivery within a session
* Sending records with an explicit **key** (keys control which partition a record lands in)
* Attaching a **callback** to handle success / failure asynchronously
* Blocking on `Future.get()` for synchronous confirmation
* Calling `producer.flush()` before closing to avoid data loss

Run it:

```bash
mvn compile exec:java -Dexec.mainClass="com.kafka.producer.ProducerDemo"
```

### 6.2 Consumer

**File:** `src/main/java/com/kafka/consumer/ConsumerDemo.java`

Key concepts demonstrated:

* Creating a `KafkaConsumer` belonging to a **consumer group** (`group.id`)
* `auto.offset.reset=earliest` – start from the beginning when no committed offset exists
* The **poll loop** – Kafka uses a push-pull model; the consumer drives fetching
* `auto.commit=true` – offsets are periodically committed in the background
* **Graceful shutdown** using `consumer.wakeup()` from a JVM shutdown hook

Run it (in a second terminal while the producer is running):

```bash
mvn compile exec:java -Dexec.mainClass="com.kafka.consumer.ConsumerDemo"
```

### 6.3 AdminClient

**File:** `src/main/java/com/kafka/admin/AdminClientDemo.java`

Key concepts demonstrated:

* Creating topics programmatically with `AdminClient.createTopics()`
* Handling `TopicExistsException` gracefully
* Listing and describing topics
* Deleting topics

Run it:

```bash
mvn compile exec:java -Dexec.mainClass="com.kafka.admin.AdminClientDemo"
```

### 6.4 Kafka Streams – Word Count

**File:** `src/main/java/com/kafka/streams/WordCountApp.java`

Key concepts demonstrated:

* **High-level Streams DSL** (`StreamsBuilder`, `KStream`, `KTable`)
* Stateless operations: `flatMapValues`, `filter`, `mapValues`
* Stateful aggregation: `groupBy` + `count` (backed by a local RocksDB state store)
* Converting a `KTable` back to a `KStream` and writing to an output topic
* Topology description for visualising the processing graph

Create the required topics, then run:

```bash
docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic word-count-input --partitions 1 --replication-factor 1

docker exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic word-count-output --partitions 1 --replication-factor 1

mvn compile exec:java -Dexec.mainClass="com.kafka.streams.WordCountApp"
```

Send test sentences (in another terminal):

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic word-count-input
# type: hello kafka hello world
```

Read the word counts:

```bash
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic word-count-output \
  --from-beginning \
  --property print.key=true
```

---

## 7. Running the Tests

All tests run **without a real Kafka broker** using:

| Test utility | Used for |
|---|---|
| `MockProducer` | Unit-testing producer logic |
| `MockConsumer` | Unit-testing consumer logic |
| `TopologyTestDriver` | Unit-testing Kafka Streams topologies |

```bash
mvn test
```

Expected output:

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  # ProducerDemoTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0  # ConsumerDemoTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  # WordCountAppTest
[INFO] BUILD SUCCESS
```

---

## 8. Building the Project

```bash
# Compile
mvn compile

# Package (produces target/java-kafka-1.0.0.jar)
mvn package -DskipTests

# Compile + test
mvn verify
```

---

## 9. Key Configuration Properties

### Producer

| Property | Default | Purpose |
|---|---|---|
| `bootstrap.servers` | – | Comma-separated list of broker host:port pairs |
| `key.serializer` | – | Converts the key object to bytes |
| `value.serializer` | – | Converts the value object to bytes |
| `acks` | `all` (with idempotence) | Number of broker acknowledgements required |
| `enable.idempotence` | `false` | Guarantees exactly-once within a producer session |
| `retries` | `Integer.MAX_VALUE` | Automatic retry on transient failures |
| `linger.ms` | `0` | Batching delay – higher values improve throughput |
| `batch.size` | `16384` | Maximum batch size in bytes |

### Consumer

| Property | Default | Purpose |
|---|---|---|
| `bootstrap.servers` | – | Same as producer |
| `group.id` | – | Consumer group identifier |
| `key.deserializer` | – | Converts bytes back to key object |
| `value.deserializer` | – | Converts bytes back to value object |
| `auto.offset.reset` | `latest` | Where to start reading when no committed offset exists |
| `enable.auto.commit` | `true` | Periodically commit offsets automatically |
| `max.poll.records` | `500` | Maximum records returned per `poll()` call |

### Kafka Streams

| Property | Default | Purpose |
|---|---|---|
| `application.id` | – | Unique identifier; also used as consumer group ID |
| `bootstrap.servers` | – | Same as producer/consumer |
| `default.key.serde` | – | Default Serde for keys |
| `default.value.serde` | – | Default Serde for values |
| `commit.interval.ms` | `30000` | How often to commit stream processor positions |
| `state.dir` | `/tmp/kafka-streams` | Local directory for state stores |

---

## 10. Further Reading

* [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
* [Kafka Streams Developer Guide](https://kafka.apache.org/documentation/streams/)
* [Confluent Developer Tutorials](https://developer.confluent.io/tutorials/)
* [Kafka: The Definitive Guide (O'Reilly)](https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/) – free edition available

