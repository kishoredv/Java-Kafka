# Java-Kafka Multi-Module Application

A comprehensive Spring Boot application demonstrating the **Outbox Pattern** with Kafka for reliable event-driven architecture. This application processes transaction files using Spring Batch and publishes events to Kafka using the Outbox pattern.

## 📋 Table of Contents
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [How It Works](#how-it-works)
- [API Documentation](#api-documentation)
- [Kafka Topics](#kafka-topics)
- [Database Schema](#database-schema)
- [Testing](#testing)

## 🏗️ Architecture

```
                    File Parser
                (Header / Body / Trailer)
                         |
                         v
                 Batch Processing
               /                   \
              v                     v
      TRANSACTION TABLE        OUTBOX TABLE
                                    |
                                    v
                         Kafka Publisher (Polling)
                                    |
                                    v
                              Kafka Topic
                          (transaction-events)
                                    |
                                    v
                           Kafka Consumer
                         (Manual Acknowledgment)
```

## 📁 Project Structure

```
Java-Kafka/
├── common-models/                 # Shared entities and DTOs
│   └── src/main/java/com/example/kafka/
│       ├── model/
│       │   ├── Transaction.java      # Transaction entity
│       │   └── OutboxEvent.java      # Outbox event entity
│       └── dto/
│           ├── TransactionEvent.java # Transaction event DTO
│           ├── FileHeader.java       # File header DTO
│           └── FileTrailer.java      # File trailer DTO
│
├── file-processor-service/        # File parsing & batch processing
│   └── src/main/java/com/example/kafka/processor/
│       ├── batch/
│       │   ├── TransactionFileReader.java    # Custom file reader
│       │   ├── TransactionItemProcessor.java # Transaction validator
│       │   └── TransactionItemWriter.java    # Dual-write to DB
│       ├── config/
│       │   └── BatchConfiguration.java       # Spring Batch config
│       ├── controller/
│       │   └── FileProcessorController.java  # REST API
│       └── repository/
│           ├── TransactionRepository.java
│           └── OutboxEventRepository.java
│
├── kafka-publisher-service/       # Outbox pattern publisher
│   └── src/main/java/com/example/kafka/publisher/
│       ├── service/
│       │   └── OutboxPublisherService.java   # Scheduled publisher
│       ├── config/
│       │   └── KafkaProducerConfig.java      # Kafka config
│       └── repository/
│           └── OutboxEventRepository.java
│
├── kafka-consumer-service/        # Event consumer
│   └── src/main/java/com/example/kafka/consumer/
│       ├── listener/
│       │   └── TransactionEventListener.java # Kafka listener
│       └── config/
│           └── KafkaConsumerConfig.java      # Consumer config
│
├── input/
│   └── transactions.txt           # Sample transaction file
├── docker-compose.yml             # Kafka & Zookeeper setup
└── pom.xml                        # Parent POM
```

## ✨ Features

### 1. **File Parser Module**
- Parses Header/Body/Trailer format transaction files
- Validates transaction data (card number, amount, etc.)
- Spring Batch chunk-oriented processing
- Error handling and logging

### 2. **Outbox Pattern Implementation**
- **Transactional Dual-Write**: Saves Transaction and OutboxEvent in same DB transaction
- Prevents data loss and ensures consistency
- Solves the dual-write problem (database + message broker)

### 3. **Kafka Publisher**
- Scheduled polling (every 5 seconds) for unprocessed outbox events
- Idempotent publishing with retry logic (max 3 retries)
- Marks events as processed after successful publish
- Error tracking and logging

### 4. **Kafka Consumer**
- Manual acknowledgment for reliability
- Consumer group support for scalability
- Deserializes JSON events
- Business logic processing with logging

### 5. **Database Schema**
- **Transactions Table**: Stores transaction details
- **Outbox Events Table**: Queue for Kafka publishing
- H2 in-memory database for easy testing

## 🛠️ Technologies Used

- **Spring Boot 3.2.4** - Framework
- **Spring Batch** - File processing
- **Spring Data JPA** - Database access
- **Spring Kafka 3.1.4** - Kafka integration
- **Apache Kafka** - Event streaming
- **H2 Database** - In-memory database
- **Lombok** - Boilerplate reduction
- **Jackson** - JSON serialization
- **Docker Compose** - Infrastructure setup
- **Maven** - Build tool

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker & Docker Compose** (for Kafka)

## 🚀 Getting Started

### 📖 New to the Project? Start Here!

**For detailed step-by-step local execution:**
- 🎯 **[LOCAL_EXECUTION_GUIDE.md](LOCAL_EXECUTION_GUIDE.md)** - Complete walkthrough with every single step
- 🎬 **[EXECUTION_FLOWCHART.md](EXECUTION_FLOWCHART.md)** - Visual flow diagram of the execution process

### Quick Start

### Step 1: Start Kafka Infrastructure

```bash
# Navigate to project directory
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka

# Start Kafka, Zookeeper, and Kafka UI
docker-compose up -d

# Verify containers are running
docker-compose ps
```

**Services:**
- Kafka Broker: `localhost:9092`
- Zookeeper: `localhost:2181`
- Kafka UI: `http://localhost:8080` (Web interface)

### Step 2: Build the Project

```bash
# Build all modules
mvn clean install
```

### Step 3: Start All Services

Open 3 separate terminals and run:

**Terminal 1 - File Processor Service:**
```bash
cd file-processor-service
mvn spring-boot:run
```
- **Port**: 8081
- **H2 Console**: http://localhost:8081/h2-console

**Terminal 2 - Kafka Publisher Service:**
```bash
cd kafka-publisher-service
mvn spring-boot:run
```
- **Port**: 8082
- **H2 Console**: http://localhost:8082/h2-console

**Terminal 3 - Kafka Consumer Service:**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```
- **Port**: 8083

### Step 4: Process Transaction File

Trigger file processing via REST API:

```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Expected Response:**
```
File processing job started successfully
```

## 🔄 How It Works

### End-to-End Flow:

1. **File Processing**
   - REST API triggers Spring Batch job
   - `TransactionFileReader` reads `input/transactions.txt`
   - Parses Header (batch metadata), Body (transactions), Trailer (totals)
   - `TransactionItemProcessor` validates each transaction
   - `TransactionItemWriter` performs dual-write:
     - Saves to `TRANSACTIONS` table
     - Saves to `OUTBOX_EVENTS` table (same transaction)

2. **Kafka Publishing**
   - `OutboxPublisherService` polls every 5 seconds
   - Queries unprocessed events (`processed_at IS NULL`)
   - Publishes to Kafka topic `transaction-events`
   - Marks event as processed on success
   - Retries up to 3 times on failure

3. **Event Consumption**
   - `TransactionEventListener` consumes from Kafka
   - Processes business logic (fraud detection, notifications, etc.)
   - Manual acknowledgment after successful processing
   - Logs transaction details

### Sample Transaction File Format:

```
HDR,BATCH001,2026-04-13,CORE_SYSTEM
TXN,PURCHASE,1234567890123456,ACC001,150.75
TXN,WITHDRAWAL,9876543210987654,ACC002,500.00
TXN,DEPOSIT,1111222233334444,ACC003,1000.00
TRL,10,5941.49
```

**Format:**
- **Header**: `HDR,{batchId},{date},{system}`
- **Transaction**: `TXN,{type},{cardNumber},{accountNumber},{amount}`
- **Trailer**: `TRL,{recordCount},{totalAmount}`

## 📡 API Documentation

### File Processor Service (Port 8081)

#### Process Transaction File
```http
POST /api/file-processor/process
```

**Response:**
```json
{
  "message": "File processing job started successfully"
}
```

### H2 Console Access

**File Processor DB:**
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:fileprocessordb`
- Username: `sa`
- Password: _(empty)_

**Publisher DB:**
- URL: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:publisherdb`
- Username: `sa`
- Password: _(empty)_

## 📊 Kafka Topics

### `transaction-events`

**Key:** Transaction ID (Long)  
**Value:** JSON payload

**Sample Event:**
```json
{
  "transactionId": 1,
  "transactionType": "PURCHASE",
  "cardNumber": "************3456",
  "accountNumber": "ACC001",
  "amount": 150.75,
  "batchId": "BATCH001",
  "status": "PROCESSED",
  "timestamp": "2026-04-13T10:30:00",
  "eventType": "TRANSACTION_CREATED"
}
```

**View in Kafka UI**: http://localhost:8080

## 🗄️ Database Schema

### TRANSACTIONS Table
```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_type VARCHAR(50) NOT NULL,
    card_number VARCHAR(16) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    batch_id VARCHAR(50),
    status VARCHAR(20)
);
```

### OUTBOX_EVENTS Table
```sql
CREATE TABLE outbox_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id BIGINT NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    error_message TEXT,
    INDEX idx_processed_at (processed_at),
    INDEX idx_created_at (created_at)
);
```

## 🧪 Testing

### Verify the Complete Flow:

1. **Check File Processing Logs:**
```bash
# Terminal 1 (File Processor)
# Look for: "Saved transaction: {id}"
# Look for: "Created outbox event: {id}"
```

2. **Query Transactions in H2:**
```sql
-- Connect to http://localhost:8081/h2-console
SELECT * FROM TRANSACTIONS;
SELECT * FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NULL;
```

3. **Monitor Kafka Publisher Logs:**
```bash
# Terminal 2 (Publisher)
# Look for: "Found {N} pending outbox events to publish"
# Look for: "Successfully published event {id}"
```

4. **Check Kafka Topic in UI:**
- Visit: http://localhost:8080
- Navigate to Topics → `transaction-events`
- View messages

5. **Verify Consumer Processing:**
```bash
# Terminal 3 (Consumer)
# Look for: "Received message from partition"
# Look for: "Successfully processed and acknowledged transaction event"
```

### Load Testing:

Add more transactions to `input/transactions.txt` and run:
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

## 🎯 Key Benefits of Outbox Pattern

1. **Atomicity**: Database writes and event publishing are atomic
2. **Reliability**: No lost events even if Kafka is down
3. **Consistency**: Transactions and events are always in sync
4. **Retry Logic**: Failed publishes are retried automatically
5. **Scalability**: Consumer groups enable parallel processing

## 🔧 Configuration

### Customize Polling Interval (Publisher):
```yaml
# kafka-publisher-service/src/main/resources/application.yml
outbox:
  polling:
    interval: 5000  # milliseconds
```

### Customize File Path:
```yaml
# file-processor-service/src/main/resources/application.yml
file:
  input:
    path: input/transactions.txt
```

### Kafka Configuration:
```yaml
# All services
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

## 📝 License

This project is created for educational purposes to demonstrate Kafka and Outbox pattern implementation.

## 🤝 Contributing

Feel free to fork, improve, and submit pull requests!

## 📧 Support

For issues and questions, please create a GitHub issue.

---

**Built with ❤️ using Spring Boot and Apache Kafka**
