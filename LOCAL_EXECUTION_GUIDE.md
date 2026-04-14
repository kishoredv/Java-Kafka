# 📖 Step-by-Step Local Execution Guide

This guide walks you through **every single step** to run the Java-Kafka application on your local machine from scratch.

---

## 📋 Prerequisites Check

### Step 1: Verify Java Installation

```bash
java -version
```

**Expected Output:**
```
openjdk version "17.0.x" or higher
```

**If not installed:**
- macOS: `brew install openjdk@17`
- Linux: `sudo apt-get install openjdk-17-jdk`
- Windows: Download from [Adoptium](https://adoptium.net/)

### Step 2: Verify Maven Installation

```bash
mvn -version
```

**Expected Output:**
```
Apache Maven 3.6.x or higher
```

**If not installed:**
- macOS: `brew install maven`
- Linux: `sudo apt-get install maven`
- Windows: Download from [Maven Website](https://maven.apache.org/download.cgi)

### Step 3: Verify Docker Installation

```bash
docker --version
docker ps
```

**Expected Output:**
```
Docker version 20.x.x or higher
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
```

**If not installed:**
- Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Make sure Docker Desktop is **running** (you should see the Docker icon in your system tray)

### Step 4: Verify Docker Compose

```bash
docker-compose --version
```

**OR** (newer Docker versions):
```bash
docker compose version
```

**Expected Output:**
```
Docker Compose version v2.x.x or higher
```

---

## 🚀 Part 1: Setup Kafka Infrastructure

### Step 5: Navigate to Project Directory

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
```

**Verify you're in the right place:**
```bash
ls -l
```

**You should see:**
- `docker-compose.yml`
- `pom.xml`
- Various `.md` files
- Directories: `common-models/`, `file-processor-service/`, etc.

### Step 6: Start Kafka & Zookeeper with Docker Compose

```bash
docker-compose up -d
```

**What this does:**
- Starts Zookeeper (port 2181)
- Starts Kafka Broker (port 9092)
- Starts Kafka UI (port 8080)
- All run in background (`-d` flag)

**Expected Output:**
```
Creating network "java-kafka_kafka-network" with driver "bridge"
Creating zookeeper ... done
Creating kafka     ... done
Creating kafka-ui  ... done
```

### Step 7: Verify Containers are Running

```bash
docker-compose ps
```

**Expected Output:**
```
NAME        STATE       PORTS
kafka       Up          0.0.0.0:9092->9092/tcp
zookeeper   Up          0.0.0.0:2181->2181/tcp
kafka-ui    Up          0.0.0.0:8080->8080/tcp
```

**All containers should show "Up" status.**

### Step 8: Wait for Kafka to Initialize

```bash
echo "Waiting 30 seconds for Kafka to fully start..."
sleep 30
```

**Why wait?** Kafka needs time to initialize topics and connect to Zookeeper.

### Step 9: Verify Kafka UI is Accessible

Open your browser and go to:
```
http://localhost:8080
```

**You should see:**
- Kafka UI dashboard
- "Clusters" section showing "local" cluster
- No topics yet (that's okay!)

**If page doesn't load:** Wait another 15 seconds and refresh.

---

## 🔨 Part 2: Build the Application

### Step 10: Clean and Build All Modules

```bash
mvn clean install
```

**What this does:**
- Cleans previous builds
- Compiles all 4 modules
- Downloads dependencies
- Creates JAR files

**This will take 2-5 minutes on first run** (downloading dependencies).

**Expected Output (at the end):**
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] ------------------------------------------------------------------------
[INFO] Java Kafka Multi-Module Application ............ SUCCESS [  0.xxx s]
[INFO] Common Models .................................. SUCCESS [  x.xxx s]
[INFO] File Processor Service ......................... SUCCESS [  x.xxx s]
[INFO] Kafka Publisher Service ........................ SUCCESS [  x.xxx s]
[INFO] Kafka Consumer Service ......................... SUCCESS [  x.xxx s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**If build fails:**
- Check Java version: `java -version` (must be 17+)
- Check Maven settings: `mvn --version`
- Try: `mvn clean install -U` (force update dependencies)

---

## 🎬 Part 3: Start the Services

You need **3 separate terminal windows/tabs** open.

### Step 11: Start File Processor Service (Terminal 1)

**Open Terminal 1:**
```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/file-processor-service
mvn spring-boot:run
```

**Wait for startup (30-60 seconds).**

**Expected Output (at the end):**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Started FileProcessorApplication in x.xxx seconds
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Tomcat started on port(s): 8081 (http)
```

**Success Indicator:** Look for "Tomcat started on port(s): 8081"

**Leave this terminal running!**

### Step 12: Verify File Processor is Running

**Open a NEW terminal and run:**
```bash
curl http://localhost:8081/actuator/health
```

**OR just check if port is open:**
```bash
nc -zv localhost 8081
```

**Expected:** Connection successful

### Step 13: Start Kafka Publisher Service (Terminal 2)

**Open Terminal 2:**
```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-publisher-service
mvn spring-boot:run
```

**Wait for startup (30-60 seconds).**

**Expected Output (at the end):**
```
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Started KafkaPublisherApplication in x.xxx seconds
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Tomcat started on port(s): 8082 (http)
```

**You should also see (every 5 seconds):**
```
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Found 0 pending outbox events to publish
```

**This means the scheduled polling is working!**

**Leave this terminal running!**

### Step 14: Start Kafka Consumer Service (Terminal 3)

**Open Terminal 3:**
```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-consumer-service
mvn spring-boot:run
```

**Wait for startup (30-60 seconds).**

**Expected Output (at the end):**
```
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Started KafkaConsumerApplication in x.xxx seconds
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : Tomcat started on port(s): 8083 (http)
2026-04-13 xx:xx:xx.xxx  INFO xxxxx : partitions assigned: [transaction-events-0]
```

**Success Indicator:** Look for "partitions assigned" - this means consumer connected to Kafka!

**Leave this terminal running!**

---

## ✅ Part 4: Verify All Services are Running

### Step 15: Check All Ports

**Open a NEW terminal (Terminal 4) and run:**

```bash
# Check File Processor (8081)
nc -zv localhost 8081

# Check Kafka Publisher (8082)
nc -zv localhost 8082

# Check Kafka Consumer (8083)
nc -zv localhost 8083

# Check Kafka (9092)
nc -zv localhost 9092

# Check Kafka UI (8080)
nc -zv localhost 8080
```

**All should return "succeeded" or "open"**

### Step 16: Open All Web Interfaces

**Open these URLs in your browser:**

1. **Kafka UI:** http://localhost:8080
   - You should see the Kafka dashboard
   - Click "Topics" - should be empty for now

2. **H2 Console (File Processor):** http://localhost:8081/h2-console
   - **JDBC URL:** `jdbc:h2:mem:fileprocessordb`
   - **Username:** `sa`
   - **Password:** (leave empty)
   - Click "Connect"
   - You should see tables: `TRANSACTIONS`, `OUTBOX_EVENTS`, `BATCH_*`

3. **H2 Console (Publisher):** http://localhost:8082/h2-console
   - **JDBC URL:** `jdbc:h2:tcp://localhost:9093/mem:fileprocessordb`
   - **Username:** `sa`
   - **Password:** (leave empty)
   - Click "Connect"
   - You should see the SAME database as file processor!

---

## 🎯 Part 5: Process Transaction File

### Step 17: Verify Sample File Exists

```bash
cat /Users/mannan/Desktop/SpringBoot/Java-Kafka/input/transactions.txt
```

**You should see:**
```
HDR,BATCH001,2026-04-13,CORE_SYSTEM
TXN,PURCHASE,1234567890123456,ACC001,150.75
TXN,WITHDRAWAL,9876543210987654,ACC002,500.00
...
TRL,10,5941.49
```

### Step 18: Trigger File Processing

**Run this command:**
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Expected Response:**
```
File processing job started successfully
```

### Step 19: Watch the Logs in Real-Time

Now watch what happens in each terminal:

**Terminal 1 (File Processor) - Watch for:**
```
INFO  : Processed header: FileHeader(recordType=HDR, batchId=BATCH001, ...)
INFO  : Saved transaction: 1
INFO  : Created outbox event: 1
INFO  : Saved transaction: 2
INFO  : Created outbox event: 2
...
INFO  : Processed trailer: FileTrailer(recordType=TRL, totalRecords=10, ...)
```

**Terminal 2 (Kafka Publisher) - Wait 5 seconds, then watch for:**
```
INFO  : Found 10 pending outbox events to publish
INFO  : Publishing event 1 to Kafka topic: transaction-events
INFO  : Successfully published event 1 to partition 0 with offset 0
INFO  : Publishing event 2 to Kafka topic: transaction-events
INFO  : Successfully published event 2 to partition 0 with offset 1
...
```

**Terminal 3 (Kafka Consumer) - Watch for:**
```
INFO  : Received message from partition 0 with offset 0
INFO  : Processing Transaction Event:
INFO  :   Transaction ID: 1
INFO  :   Transaction Type: PURCHASE
INFO  :   Card Number: ************3456
INFO  :   Account Number: ACC001
INFO  :   Amount: 150.75
INFO  : Transaction processing completed for ID: 1
INFO  : Successfully processed and acknowledged transaction event: 1
...
```

**🎉 If you see all these logs, everything is working perfectly!**

---

## 🔍 Part 6: Verify Data in Database

### Step 20: Check Transactions in Database

**Go to:** http://localhost:8081/h2-console

**Run this query:**
```sql
SELECT * FROM TRANSACTIONS;
```

**Expected Result:**
```
ID  TRANSACTION_TYPE  CARD_NUMBER        ACCOUNT_NUMBER  AMOUNT   BATCH_ID    STATUS
1   PURCHASE          1234567890123456   ACC001          150.75   BATCH001    PROCESSED
2   WITHDRAWAL        9876543210987654   ACC002          500.00   BATCH001    PROCESSED
...
(10 rows)
```

### Step 21: Check Outbox Events

**In the same H2 console, run:**
```sql
SELECT ID, AGGREGATE_ID, EVENT_TYPE, CREATED_AT, PROCESSED_AT 
FROM OUTBOX_EVENTS
ORDER BY ID;
```

**Expected Result:**
```
ID  AGGREGATE_ID  EVENT_TYPE             CREATED_AT              PROCESSED_AT
1   1             TRANSACTION_CREATED    2026-04-13 10:00:00     2026-04-13 10:00:05
2   2             TRANSACTION_CREATED    2026-04-13 10:00:00     2026-04-13 10:00:05
...
(10 rows with PROCESSED_AT populated)
```

**✅ All events should have PROCESSED_AT timestamps!**

### Step 22: Check Pending Events (Should be empty)

```sql
SELECT COUNT(*) FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NULL;
```

**Expected Result:**
```
COUNT
0
```

---

## 📊 Part 7: Verify in Kafka UI

### Step 23: View Messages in Kafka UI

1. **Open:** http://localhost:8080

2. **Click on "Topics" in left menu**

3. **Click on "transaction-events" topic**

4. **Click on "Messages" tab**

**You should see:**
- **Total Messages:** 10
- **Partitions:** 1 (partition 0)
- List of 10 messages with offsets 0-9

5. **Click on any message to view details**

**You should see:**
```json
{
  "transactionId": 1,
  "transactionType": "PURCHASE",
  "cardNumber": "************3456",
  "accountNumber": "ACC001",
  "amount": 150.75,
  "batchId": "BATCH001",
  "status": "PROCESSED",
  "timestamp": "2026-04-13T10:00:00",
  "eventType": "TRANSACTION_CREATED"
}
```

### Step 24: Check Consumer Group

1. **In Kafka UI, click "Consumers" in left menu**

2. **Find "transaction-consumer-group"**

3. **You should see:**
   - **Status:** Active
   - **Members:** 3 (concurrent consumers)
   - **Lag:** 0 (all messages consumed)

**✅ Lag = 0 means consumer has processed all messages!**

---

## 🎉 Success Checklist

After completing all steps, verify:

- [ ] ✅ All 3 Spring Boot services running without errors
- [ ] ✅ Docker containers (kafka, zookeeper, kafka-ui) are running
- [ ] ✅ File processor logs show "Saved transaction" messages
- [ ] ✅ Publisher logs show "Successfully published event" messages
- [ ] ✅ Consumer logs show "Successfully processed and acknowledged" messages
- [ ] ✅ H2 database has 10 transactions in TRANSACTIONS table
- [ ] ✅ H2 database has 10 events in OUTBOX_EVENTS table
- [ ] ✅ All outbox events have PROCESSED_AT timestamps
- [ ] ✅ Kafka UI shows 10 messages in transaction-events topic
- [ ] ✅ Consumer group shows lag = 0

---

## 🔄 Part 8: Run Again (Optional)

Want to process the file again?

### Step 25: Process File Again

```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**What happens:**
- 10 more transactions created (IDs 11-20)
- 10 more outbox events created
- Events published to Kafka
- Consumer processes new events
- **Total messages in Kafka: 20**

---

## 🛑 Part 9: Stop the Application

### Step 26: Stop Spring Boot Services

**In each terminal (1, 2, 3), press:**
```
Ctrl + C
```

**Wait for graceful shutdown:**
```
INFO  : Stopping service [Tomcat]
INFO  : Application shutdown complete
```

### Step 27: Stop Docker Containers

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
docker-compose down
```

**Expected Output:**
```
Stopping kafka-ui  ... done
Stopping kafka     ... done
Stopping zookeeper ... done
Removing kafka-ui  ... done
Removing kafka     ... done
Removing zookeeper ... done
Removing network java-kafka_kafka-network
```

### Step 28: Verify Everything Stopped

```bash
docker-compose ps
```

**Expected Output:**
```
NAME                COMMAND             STATE      PORTS
(empty - no containers running)
```

---

## 🔧 Troubleshooting Common Issues

### Issue 1: "Port 8081 already in use"

**Problem:** Another application is using the port.

**Solution:**
```bash
# Find what's using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or change the port in application.yml
```

### Issue 2: "Connection refused" when accessing Kafka

**Problem:** Kafka not fully started yet.

**Solution:**
```bash
# Wait longer
sleep 30

# Check Kafka logs
docker-compose logs kafka

# Restart Kafka
docker-compose restart kafka
```

### Issue 3: "BUILD FAILURE" when running mvn clean install

**Problem:** Wrong Java version or Maven issues.

**Solution:**
```bash
# Check Java version
java -version  # Should be 17+

# Clean Maven cache
rm -rf ~/.m2/repository

# Try again
mvn clean install -U
```

### Issue 4: Services start but no logs appear

**Problem:** Wrong directory or service not started properly.

**Solution:**
```bash
# Make sure you're in the right directory
pwd  # Should show: .../Java-Kafka/file-processor-service

# Check if port is actually open
nc -zv localhost 8081

# Restart the service
# Press Ctrl+C, then run mvn spring-boot:run again
```

### Issue 5: H2 Console shows "Database not found"

**Problem:** Wrong JDBC URL or service not running.

**Solution:**
```bash
# Verify service is running
curl http://localhost:8081/actuator/health

# Use exact JDBC URL: jdbc:h2:mem:fileprocessordb
# Username: sa
# Password: (leave empty)
```

### Issue 6: No messages in Kafka UI

**Problem:** Processing not triggered or publisher not running.

**Solution:**
```bash
# 1. Check if file processor is running (Terminal 1)
# 2. Check if publisher is running (Terminal 2)
# 3. Trigger processing again
curl -X POST http://localhost:8081/api/file-processor/process

# 4. Wait 5 seconds for publisher to poll
# 5. Check Kafka UI again
```

---

## 📚 What Each Component Does

### File Processor Service (Port 8081)
- **Reads:** `input/transactions.txt`
- **Parses:** Header, Body (transactions), Trailer
- **Validates:** Card numbers, amounts, etc.
- **Writes:** To TRANSACTIONS and OUTBOX_EVENTS tables (atomic)
- **Database:** H2 in-memory

### Kafka Publisher Service (Port 8082)
- **Polls:** OUTBOX_EVENTS table every 5 seconds
- **Finds:** Unprocessed events (PROCESSED_AT IS NULL)
- **Publishes:** Events to Kafka topic "transaction-events"
- **Updates:** Sets PROCESSED_AT timestamp on success
- **Retries:** Up to 3 times on failure

### Kafka Consumer Service (Port 8083)
- **Listens:** To Kafka topic "transaction-events"
- **Receives:** JSON event messages
- **Processes:** Business logic (currently just logging)
- **Acknowledges:** Manually after successful processing
- **Consumer Group:** "transaction-consumer-group"

### Kafka Infrastructure
- **Zookeeper (Port 2181):** Coordinates Kafka brokers
- **Kafka Broker (Port 9092):** Message storage and routing
- **Kafka UI (Port 8080):** Web interface for monitoring

---

## 🎯 Quick Command Reference

```bash
# Start everything
docker-compose up -d
mvn clean install
# Then start 3 services in 3 terminals

# Trigger processing
curl -X POST http://localhost:8081/api/file-processor/process

# Check database
# Open http://localhost:8081/h2-console
# Run: SELECT * FROM TRANSACTIONS;

# View Kafka messages
# Open http://localhost:8080

# Stop everything
# Press Ctrl+C in each terminal
docker-compose down
```

---

## ✅ You're Done!

**Congratulations!** 🎉 You've successfully:
- ✅ Set up Kafka infrastructure
- ✅ Built the multi-module application
- ✅ Started all 3 microservices
- ✅ Processed a transaction file
- ✅ Verified data in database
- ✅ Monitored Kafka messages
- ✅ Confirmed end-to-end flow

**You now understand how the Outbox pattern works in a real Spring Boot + Kafka application!**

---

## 📖 Next Steps

1. **Modify the transaction file** (`input/transactions.txt`) and process it again
2. **Study the source code** to understand implementation details
3. **Change configurations** (polling interval, ports, etc.)
4. **Add your own business logic** in the consumer
5. **Read the architecture documentation** for design decisions

**Happy Learning! 🚀**

