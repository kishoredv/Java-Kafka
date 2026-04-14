# Architecture & Flow Diagrams

## 🏗️ System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                      Java-Kafka Application                      │
│                                                                   │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│  │ File Processor │  │Kafka Publisher │  │ Kafka Consumer   │  │
│  │   Service      │  │    Service     │  │    Service       │  │
│  │   (Port 8081)  │  │  (Port 8082)   │  │   (Port 8083)    │  │
│  └────────────────┘  └────────────────┘  └──────────────────┘  │
│          │                   │                      │            │
│          ▼                   ▼                      │            │
│  ┌────────────────┐  ┌────────────────┐            │            │
│  │  H2 Database   │  │  H2 Database   │            │            │
│  │ (FileProcessor)│  │  (Publisher)   │            │            │
│  └────────────────┘  └────────────────┘            │            │
└───────────────────────────────────────────────────┼─────────────┘
                                                      │
                        ┌─────────────────────────────┘
                        │
            ┌───────────▼──────────┐
            │   Apache Kafka       │
            │   (Port 9092)        │
            │                      │
            │  Topic:              │
            │  transaction-events  │
            └──────────────────────┘
                        │
            ┌───────────▼──────────┐
            │   Kafka UI           │
            │   (Port 8080)        │
            └──────────────────────┘
```

## 📊 Data Flow Diagram

### Complete Transaction Flow
```
┌─────────────────────────────────────────────────────────────────────┐
│ STEP 1: File Processing                                             │
└─────────────────────────────────────────────────────────────────────┘

input/transactions.txt
         │
         │ HDR,BATCH001,2026-04-13,CORE_SYSTEM
         │ TXN,PURCHASE,1234567890123456,ACC001,150.75
         │ TXN,WITHDRAWAL,9876543210987654,ACC002,500.00
         │ TRL,2,650.75
         │
         ▼
┌─────────────────────────┐
│ TransactionFileReader   │  ← Parses Header/Body/Trailer
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ TransactionItemProcessor│  ← Validates transactions
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ TransactionItemWriter   │  ← Dual-write (same transaction)
└────────────┬────────────┘
             │
         ┌───┴────┐
         │        │
         ▼        ▼
┌──────────────┐ ┌──────────────┐
│ TRANSACTIONS │ │OUTBOX_EVENTS │
│    TABLE     │ │    TABLE     │
└──────────────┘ └──────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ STEP 2: Kafka Publishing (Outbox Pattern)                           │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────────────┐
│ OutboxPublisherService   │
│                          │
│ @Scheduled(5 seconds)    │  ← Polls every 5 seconds
└────────────┬─────────────┘
             │
             │ SELECT * FROM OUTBOX_EVENTS
             │ WHERE processed_at IS NULL
             │ AND retry_count < 3
             │
             ▼
┌────────────────────────────────┐
│ OUTBOX_EVENTS TABLE            │
│                                │
│ id | aggregate_id | payload    │
│  1 |      1       | {...}      │
│  2 |      2       | {...}      │
└────────────┬───────────────────┘
             │
             │ Publish to Kafka
             │
             ▼
┌──────────────────────────────────┐
│ Apache Kafka                     │
│                                  │
│ Topic: transaction-events        │
│ Partition 0: [msg1, msg2, ...]  │
└────────────┬─────────────────────┘
             │
             │ On success
             │
             ▼
┌────────────────────────────────┐
│ UPDATE OUTBOX_EVENTS           │
│ SET processed_at = NOW()       │
│ WHERE id = ?                   │
└────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ STEP 3: Event Consumption                                           │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────┐
│ Apache Kafka                     │
│ Topic: transaction-events        │
└────────────┬─────────────────────┘
             │
             │ Poll messages
             │
             ▼
┌────────────────────────────────┐
│ TransactionEventListener       │
│                                │
│ @KafkaListener                 │
│ Manual Acknowledgment          │
└────────────┬───────────────────┘
             │
             ▼
┌────────────────────────────────┐
│ Process Business Logic         │
│ - Deserialize JSON             │
│ - Validate event               │
│ - Execute business logic       │
│ - Log transaction details      │
└────────────┬───────────────────┘
             │
             ▼
┌────────────────────────────────┐
│ Manual Acknowledgment          │
│ acknowledgment.acknowledge()   │
└────────────────────────────────┘
```

## 🔄 Outbox Pattern Flow

```
┌────────────────────────────────────────────────────────────────┐
│                     Outbox Pattern Benefits                     │
└────────────────────────────────────────────────────────────────┘

Traditional Approach (Dual-Write Problem):
┌──────────────┐
│ Save to DB   │ ──✅──▶ Success
└──────────────┘
        │
        ▼
┌──────────────┐
│ Publish Kafka│ ──❌──▶ Failure (Kafka down)
└──────────────┘
Result: Data inconsistency! Transaction saved but event not published.

Outbox Pattern Approach:
┌──────────────────────────────────────┐
│ Database Transaction (Atomic)        │
│  ┌────────────────────────────────┐ │
│  │ 1. Save to TRANSACTIONS table  │ │ ──✅──▶ Commit
│  │ 2. Save to OUTBOX_EVENTS table │ │
│  └────────────────────────────────┘ │
└──────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────┐
│ Background Publisher (Async)         │
│  - Polls OUTBOX_EVENTS table         │
│  - Publishes to Kafka                │
│  - Retries on failure (max 3)        │
│  - Marks as processed on success     │
└──────────────────────────────────────┘

Result: ✅ No data loss! Events always published eventually.
```

## 🗄️ Database Schema Relationships

```
TRANSACTIONS Table
┌─────────────────────────────────────────┐
│ id (PK)             BIGINT              │
│ transaction_type    VARCHAR(50)         │
│ card_number         VARCHAR(16)         │
│ account_number      VARCHAR(20)         │
│ amount              DECIMAL(19,2)       │
│ created_at          TIMESTAMP           │
│ batch_id            VARCHAR(50)         │
│ status              VARCHAR(20)         │
└─────────────────────────────────────────┘
                    │
                    │ One-to-One (logical)
                    │
                    ▼
OUTBOX_EVENTS Table
┌─────────────────────────────────────────┐
│ id (PK)             BIGINT              │
│ aggregate_id (FK)   BIGINT              │──┐
│ aggregate_type      VARCHAR(50)         │  │ References
│ event_type          VARCHAR(50)         │  │ transaction.id
│ payload             TEXT (JSON)         │  │ (logical FK)
│ created_at          TIMESTAMP           │◀─┘
│ processed_at        TIMESTAMP           │
│ retry_count         INT                 │
│ error_message       TEXT                │
└─────────────────────────────────────────┘
```

## 📦 Module Dependencies

```
┌──────────────────────────────────────────────────────────────┐
│                    Parent POM (pom.xml)                       │
│                 Spring Boot 3.2.4 Parent                      │
└──────────────────┬──────────────────┬────────────────────────┘
                   │                  │
      ┌────────────┼──────────────────┼────────────────┐
      │            │                  │                 │
      ▼            ▼                  ▼                 ▼
┌──────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ common-  │ │file-processor│ │kafka-publisher│ │kafka-consumer│
│ models   │ │  -service    │ │  -service    │ │  -service    │
└────┬─────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
     │              │                │                 │
     │              │                │                 │
     └──────────────┴────────────────┴─────────────────┘
                    │
                    ▼
          Shared Dependencies:
          • Transaction.java
          • OutboxEvent.java
          • TransactionEvent.java
          • FileHeader.java
          • FileTrailer.java
```

## 🚀 Service Communication Flow

```
External Request
      │
      ▼
┌─────────────────────┐
│  REST API Call      │
│  POST /api/file     │
│  -processor/process │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────┐
│ File Processor Service  │
│ (Port 8081)             │
│                         │
│ Spring Batch Job:       │
│ 1. Read file            │
│ 2. Process transactions │
│ 3. Write to DB          │
└──────────┬──────────────┘
           │
           │ Stores data
           ▼
┌─────────────────────────┐
│ H2 Database             │
│ - TRANSACTIONS          │
│ - OUTBOX_EVENTS         │
└─────────────────────────┘
           │
           │ Polls (every 5s)
           ▼
┌─────────────────────────┐
│ Kafka Publisher Service │
│ (Port 8082)             │
│                         │
│ @Scheduled polling:     │
│ 1. Query outbox         │
│ 2. Publish to Kafka     │
│ 3. Mark as processed    │
└──────────┬──────────────┘
           │
           │ Publishes events
           ▼
┌─────────────────────────┐
│ Apache Kafka            │
│ Topic: transaction-     │
│        events           │
└──────────┬──────────────┘
           │
           │ Consumes events
           ▼
┌─────────────────────────┐
│ Kafka Consumer Service  │
│ (Port 8083)             │
│                         │
│ @KafkaListener:         │
│ 1. Receive event        │
│ 2. Process business     │
│ 3. Acknowledge message  │
└─────────────────────────┘
```

## 🔐 Transaction Management

```
File Processor Service - Dual Write Transaction:

BEGIN TRANSACTION
    │
    ├─▶ INSERT INTO transactions (
    │       transaction_type, card_number, 
    │       account_number, amount, batch_id, status
    │   ) VALUES (?, ?, ?, ?, ?, ?)
    │
    │   Returns: transaction_id = 1
    │
    ├─▶ INSERT INTO outbox_events (
    │       aggregate_id, aggregate_type, 
    │       event_type, payload, created_at
    │   ) VALUES (1, 'TRANSACTION', 
    │              'TRANSACTION_CREATED', 
    │              '{"transactionId":1,...}',
    │              NOW())
    │
    ├─▶ COMMIT
    │
    └─▶ ✅ Both inserts succeed or both fail
         Atomicity guaranteed!
```

## 📊 Message Format

### Kafka Message Structure
```json
{
  "key": "1",                              # Transaction ID
  "value": {
    "transactionId": 1,
    "transactionType": "PURCHASE",
    "cardNumber": "************3456",     # Masked for security
    "accountNumber": "ACC001",
    "amount": 150.75,
    "batchId": "BATCH001",
    "status": "PROCESSED",
    "timestamp": "2026-04-13T10:30:00",
    "eventType": "TRANSACTION_CREATED"
  },
  "headers": {
    "contentType": "application/json"
  },
  "partition": 0,
  "offset": 123
}
```

## ⚙️ Configuration Flow

```
application.yml Files:

File Processor (8081)          Publisher (8082)             Consumer (8083)
┌──────────────────┐           ┌──────────────────┐         ┌──────────────────┐
│ spring:          │           │ spring:          │         │ spring:          │
│   datasource:    │           │   datasource:    │         │   kafka:         │
│     url: h2:mem  │           │     url: h2:mem  │         │     bootstrap    │
│   batch:         │           │   kafka:         │         │     -servers:    │
│     job.enabled  │           │     bootstrap    │         │     localhost    │
│     : false      │           │     -servers:    │         │     :9092        │
│                  │           │     localhost    │         │   consumer:      │
│ file.input.path: │           │     :9092        │         │     group-id:    │
│   input/trans    │           │                  │         │     transaction  │
│   actions.txt    │           │ outbox.polling   │         │     -consumer    │
└──────────────────┘           │   .interval:5000 │         │     -group       │
                               └──────────────────┘         │     enable-auto  │
                                                            │     -commit:false│
                                                            └──────────────────┘
```

This architecture ensures:
- ✅ **Reliability**: No message loss with Outbox pattern
- ✅ **Consistency**: Atomic dual-write operations
- ✅ **Scalability**: Consumer groups for parallel processing
- ✅ **Maintainability**: Separate services with clear responsibilities
- ✅ **Observability**: Comprehensive logging and monitoring

