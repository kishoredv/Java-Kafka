# Complete Example Walkthrough

This document provides a complete step-by-step walkthrough of running the Java-Kafka application with real examples and expected outputs.

## 📋 Prerequisites Checklist

Before starting, verify:
- [ ] Java 17+ installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Docker installed (`docker --version`)
- [ ] Docker Compose installed (`docker-compose --version`)
- [ ] Ports available: 8081, 8082, 8083, 9092, 2181, 8080

## 🚀 Step-by-Step Execution

### Step 1: Start Kafka Infrastructure

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
docker-compose up -d
```

**Expected Output:**
```
Creating network "java-kafka_kafka-network" with driver "bridge"
Creating zookeeper ... done
Creating kafka     ... done
Creating kafka-ui  ... done
```

**Verify:**
```bash
docker-compose ps
```

**Expected Output:**
```
NAME        IMAGE                              STATUS        PORTS
kafka       confluentinc/cp-kafka:7.5.0       Up 30s        0.0.0.0:9092->9092/tcp
zookeeper   confluentinc/cp-zookeeper:7.5.0   Up 31s        0.0.0.0:2181->2181/tcp
kafka-ui    provectuslabs/kafka-ui:latest     Up 29s        0.0.0.0:8080->8080/tcp
```

Wait 30 seconds for Kafka to be fully ready.

### Step 2: Build the Project

```bash
mvn clean install
```

**Expected Output:**
```
[INFO] Reactor Summary:
[INFO] 
[INFO] Java Kafka Multi-Module Application ............ SUCCESS [  0.123 s]
[INFO] Common Models .................................. SUCCESS [  2.456 s]
[INFO] File Processor Service ......................... SUCCESS [  3.789 s]
[INFO] Kafka Publisher Service ........................ SUCCESS [  2.345 s]
[INFO] Kafka Consumer Service ......................... SUCCESS [  1.987 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Step 3: Start File Processor Service

**Terminal 1:**
```bash
cd file-processor-service
mvn spring-boot:run
```

**Expected Startup Logs:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-04-13 10:00:00.000  INFO  FileProcessorApplication : Starting FileProcessorApplication
2026-04-13 10:00:05.000  INFO  FileProcessorApplication : Started FileProcessorApplication in 5.234 seconds
2026-04-13 10:00:05.001  INFO  TomcatWebServer : Tomcat started on port(s): 8081 (http)
```

**Verify H2 Console:**
- Open: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:fileprocessordb`
- Username: `sa`
- Password: (leave empty)
- Click "Connect"

You should see tables:
- `TRANSACTIONS`
- `OUTBOX_EVENTS`
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_STEP_EXECUTION`

### Step 4: Start Kafka Publisher Service

**Terminal 2:**
```bash
cd kafka-publisher-service
mvn spring-boot:run
```

**Expected Startup Logs:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-04-13 10:01:00.000  INFO  KafkaPublisherApplication : Starting KafkaPublisherApplication
2026-04-13 10:01:05.000  INFO  KafkaPublisherApplication : Started KafkaPublisherApplication in 5.123 seconds
2026-04-13 10:01:05.001  INFO  TomcatWebServer : Tomcat started on port(s): 8082 (http)
2026-04-13 10:01:10.000  INFO  OutboxPublisherService : Found 0 pending outbox events to publish
```

The last line indicates the scheduled job is running and polling for events every 5 seconds.

### Step 5: Start Kafka Consumer Service

**Terminal 3:**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```

**Expected Startup Logs:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-04-13 10:02:00.000  INFO  KafkaConsumerApplication : Starting KafkaConsumerApplication
2026-04-13 10:02:05.000  INFO  KafkaConsumerApplication : Started KafkaConsumerApplication in 5.098 seconds
2026-04-13 10:02:05.001  INFO  TomcatWebServer : Tomcat started on port(s): 8083 (http)
2026-04-13 10:02:05.500  INFO  KafkaMessageListenerContainer : transaction-consumer-group: partitions assigned: [transaction-events-0]
```

The consumer is now listening to the Kafka topic!

### Step 6: Process Transaction File

**Terminal 4 (or new terminal):**
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Expected Response:**
```
File processing job started successfully
```

### Step 7: Monitor the Flow

#### **Terminal 1 (File Processor) - Expected Logs:**

```
2026-04-13 10:03:00.001  INFO  TransactionFileReader : Processed header: FileHeader(recordType=HDR, batchId=BATCH001, creationDate=2026-04-13, sourceSystem=CORE_SYSTEM)
2026-04-13 10:03:00.010  INFO  TransactionItemWriter : Saved transaction: 1
2026-04-13 10:03:00.011  INFO  TransactionItemWriter : Created outbox event: 1
2026-04-13 10:03:00.020  INFO  TransactionItemWriter : Saved transaction: 2
2026-04-13 10:03:00.021  INFO  TransactionItemWriter : Created outbox event: 2
2026-04-13 10:03:00.030  INFO  TransactionItemWriter : Saved transaction: 3
2026-04-13 10:03:00.031  INFO  TransactionItemWriter : Created outbox event: 3
...
2026-04-13 10:03:00.100  INFO  TransactionFileReader : Processed trailer: FileTrailer(recordType=TRL, totalRecords=10, totalAmount=5941.49)
2026-04-13 10:03:00.150  INFO  BatchConfiguration : Job completed successfully!
```

#### **Terminal 2 (Publisher) - Expected Logs:**

```
2026-04-13 10:03:05.000  INFO  OutboxPublisherService : Found 10 pending outbox events to publish
2026-04-13 10:03:05.050  INFO  OutboxPublisherService : Publishing event 1 to Kafka topic: transaction-events
2026-04-13 10:03:05.100  INFO  OutboxPublisherService : Successfully published event 1 to partition 0 with offset 0
2026-04-13 10:03:05.150  INFO  OutboxPublisherService : Publishing event 2 to Kafka topic: transaction-events
2026-04-13 10:03:05.200  INFO  OutboxPublisherService : Successfully published event 2 to partition 0 with offset 1
...
2026-04-13 10:03:06.000  INFO  OutboxPublisherService : Publishing event 10 to Kafka topic: transaction-events
2026-04-13 10:03:06.050  INFO  OutboxPublisherService : Successfully published event 10 to partition 0 with offset 9
2026-04-13 10:03:10.000  INFO  OutboxPublisherService : Found 0 pending outbox events to publish
```

#### **Terminal 3 (Consumer) - Expected Logs:**

```
2026-04-13 10:03:05.110  INFO  TransactionEventListener : Received message from partition 0 with offset 0
2026-04-13 10:03:05.120  INFO  TransactionEventListener : Processing Transaction Event:
2026-04-13 10:03:05.121  INFO  TransactionEventListener :   Transaction ID: 1
2026-04-13 10:03:05.122  INFO  TransactionEventListener :   Transaction Type: PURCHASE
2026-04-13 10:03:05.123  INFO  TransactionEventListener :   Card Number: ************3456
2026-04-13 10:03:05.124  INFO  TransactionEventListener :   Account Number: ACC001
2026-04-13 10:03:05.125  INFO  TransactionEventListener :   Amount: 150.75
2026-04-13 10:03:05.126  INFO  TransactionEventListener :   Batch ID: BATCH001
2026-04-13 10:03:05.127  INFO  TransactionEventListener :   Status: PROCESSED
2026-04-13 10:03:05.128  INFO  TransactionEventListener :   Timestamp: 2026-04-13T10:03:00
2026-04-13 10:03:05.230  INFO  TransactionEventListener : Transaction processing completed for ID: 1
2026-04-13 10:03:05.231  INFO  TransactionEventListener : Successfully processed and acknowledged transaction event: 1
2026-04-13 10:03:05.310  INFO  TransactionEventListener : Received message from partition 0 with offset 1
...
```

### Step 8: Verify Data in Database

**Open H2 Console:** http://localhost:8081/h2-console

**Query 1: Check all transactions**
```sql
SELECT * FROM TRANSACTIONS;
```

**Expected Result:**
```
ID  TRANSACTION_TYPE  CARD_NUMBER        ACCOUNT_NUMBER  AMOUNT   BATCH_ID    STATUS     CREATED_AT
1   PURCHASE          1234567890123456   ACC001          150.75   BATCH001    PROCESSED  2026-04-13 10:03:00.010
2   WITHDRAWAL        9876543210987654   ACC002          500.00   BATCH001    PROCESSED  2026-04-13 10:03:00.020
3   DEPOSIT           1111222233334444   ACC003          1000.00  BATCH001    PROCESSED  2026-04-13 10:03:00.030
...
```

**Query 2: Check outbox events**
```sql
SELECT ID, AGGREGATE_ID, EVENT_TYPE, CREATED_AT, PROCESSED_AT, RETRY_COUNT 
FROM OUTBOX_EVENTS;
```

**Expected Result:**
```
ID  AGGREGATE_ID  EVENT_TYPE             CREATED_AT              PROCESSED_AT            RETRY_COUNT
1   1             TRANSACTION_CREATED    2026-04-13 10:03:00     2026-04-13 10:03:05     0
2   2             TRANSACTION_CREATED    2026-04-13 10:03:00     2026-04-13 10:03:05     0
3   3             TRANSACTION_CREATED    2026-04-13 10:03:00     2026-04-13 10:03:05     0
...
```

All events should have `PROCESSED_AT` populated, indicating successful Kafka publishing!

**Query 3: Check payload of an event**
```sql
SELECT PAYLOAD FROM OUTBOX_EVENTS WHERE ID = 1;
```

**Expected Result:**
```json
{
  "transactionId": 1,
  "transactionType": "PURCHASE",
  "cardNumber": "************3456",
  "accountNumber": "ACC001",
  "amount": 150.75,
  "batchId": "BATCH001",
  "status": "PROCESSED",
  "timestamp": "2026-04-13T10:03:00",
  "eventType": "TRANSACTION_CREATED"
}
```

### Step 9: Verify in Kafka UI

**Open Kafka UI:** http://localhost:8080

**Navigation:**
1. Click on "Topics" in left menu
2. Click on "transaction-events"
3. Click on "Messages" tab

**You should see:**
- **Total Messages:** 10
- **Partitions:** 1 (partition 0)
- **Consumer Groups:** 1 (transaction-consumer-group)

**Click on a message to view:**
```json
{
  "key": "1",
  "value": {
    "transactionId": 1,
    "transactionType": "PURCHASE",
    "cardNumber": "************3456",
    "accountNumber": "ACC001",
    "amount": 150.75,
    "batchId": "BATCH001",
    "status": "PROCESSED",
    "timestamp": "2026-04-13T10:03:00",
    "eventType": "TRANSACTION_CREATED"
  },
  "partition": 0,
  "offset": 0,
  "timestamp": "2026-04-13T10:03:05.100"
}
```

**Check Consumer Groups:**
1. Click on "Consumers" in left menu
2. Find "transaction-consumer-group"
3. You should see:
   - **Status:** Active
   - **Members:** 3 (concurrent consumers)
   - **Lag:** 0 (all messages processed)

## 🧪 Testing Scenarios

### Scenario 1: Add More Transactions

**Edit the file:**
```bash
nano input/transactions.txt
```

**Add new transactions before the trailer:**
```
TXN,PURCHASE,9999888877776666,ACC011,299.99
TXN,DEPOSIT,5555444433332222,ACC012,1500.00
```

**Update trailer count and amount:**
```
TRL,12,7741.48
```

**Save and run again:**
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Watch the logs - you'll see 2 new transactions processed!**

### Scenario 2: Simulate Kafka Failure

**Stop Kafka:**
```bash
docker-compose stop kafka
```

**Trigger file processing:**
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Observe:**
- File Processor: ✅ Processes successfully, saves to DB
- Publisher: ❌ Fails to publish, retries up to 3 times
- Outbox events remain with `PROCESSED_AT = NULL`

**Restart Kafka:**
```bash
docker-compose start kafka
```

**Wait 30 seconds, then observe:**
- Publisher: ✅ Picks up pending events and publishes successfully
- Events marked as processed
- Consumer: ✅ Receives and processes events

**This demonstrates the Outbox pattern's reliability!**

### Scenario 3: Consumer Lag Simulation

**Stop the consumer (Ctrl+C in Terminal 3)**

**Process multiple files:**
```bash
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/file-processor/process
  sleep 2
done
```

**Check Kafka UI:**
- Navigate to Consumers → transaction-consumer-group
- You'll see **Lag increasing** (messages not consumed)

**Restart consumer:**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```

**Observe:**
- Consumer catches up and processes all pending messages
- Lag returns to 0

## 📊 Metrics to Monitor

### Key Performance Indicators

1. **File Processing Time**
   - Look for: "Job completed successfully" log
   - Typical: 100-500ms for 10 transactions

2. **Kafka Publishing Latency**
   - Look for: Time between "Publishing event" and "Successfully published"
   - Typical: 50-100ms per message

3. **Consumer Processing Time**
   - Look for: Time between "Received message" and "Successfully processed"
   - Typical: 100-200ms per message

4. **End-to-End Latency**
   - From file trigger to consumer acknowledgment
   - Typical: 5-10 seconds (depends on polling interval)

5. **Outbox Polling Frequency**
   - Default: Every 5 seconds
   - Adjust in `application.yml`: `outbox.polling.interval`

## 🐛 Common Issues & Solutions

### Issue 1: Port Already in Use

**Error:**
```
Web server failed to start. Port 8081 was already in use.
```

**Solution:**
```bash
# Find process using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or use a different port in application.yml
server:
  port: 8091
```

### Issue 2: Kafka Connection Refused

**Error:**
```
Connection to node -1 could not be established
```

**Solution:**
```bash
# Check if Kafka is running
docker-compose ps

# Restart Kafka
docker-compose restart kafka

# Wait 30 seconds for Kafka to be ready
```

### Issue 3: H2 Console Shows Empty Tables

**Issue:** Tables are empty after processing

**Possible Causes:**
1. Wrong database URL
2. Job failed silently
3. Using wrong H2 console (Publisher vs Processor)

**Solution:**
```bash
# Check logs for errors
# Ensure you're using the correct JDBC URL:
# File Processor: jdbc:h2:mem:fileprocessordb
# Publisher: jdbc:h2:mem:publisherdb

# Re-run the processing:
curl -X POST http://localhost:8081/api/file-processor/process
```

### Issue 4: Consumer Not Receiving Messages

**Symptoms:** Publisher logs show success, but consumer has no logs

**Solution:**
```bash
# Check consumer group in Kafka UI
# Verify topic name matches in all services: "transaction-events"

# Check consumer logs for connection errors
# Restart consumer service
```

## 🎯 Success Criteria

Your application is working correctly if you see:

✅ **File Processor:**
- "Saved transaction: {id}" logs
- "Created outbox event: {id}" logs
- "Job completed successfully"

✅ **Publisher:**
- "Found {N} pending outbox events to publish"
- "Successfully published event {id} to partition 0"
- "Found 0 pending outbox events" (after processing)

✅ **Consumer:**
- "Received message from partition"
- "Processing Transaction Event"
- "Successfully processed and acknowledged transaction event"

✅ **Database:**
- TRANSACTIONS table populated
- OUTBOX_EVENTS all have PROCESSED_AT timestamps

✅ **Kafka UI:**
- Topic "transaction-events" shows messages
- Consumer group shows lag = 0
- All messages visible in UI

## 🎉 Congratulations!

You've successfully:
1. ✅ Set up a multi-module Spring Boot application
2. ✅ Implemented the Outbox pattern for reliable messaging
3. ✅ Processed files with Spring Batch
4. ✅ Published events to Kafka
5. ✅ Consumed events with manual acknowledgment
6. ✅ Verified the complete flow

This architecture is production-ready and demonstrates industry best practices for event-driven microservices!

