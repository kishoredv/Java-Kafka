# 🚀 Quick Startup Commands for Java-Kafka Application

## 🎯 **FASTEST WAY: Use Startup Scripts (Recommended)**

We've created easy-to-use scripts that handle all Java 17 setup automatically!

### **Terminal 1: File Processor**
```bash
/Users/mannan/Desktop/SpringBoot/Java-Kafka/start-file-processor.sh
```

### **Terminal 2: Kafka Publisher (START AFTER TERMINAL 1)**
```bash
/Users/mannan/Desktop/SpringBoot/Java-Kafka/start-publisher.sh
```

### **Terminal 3: Kafka Consumer**
```bash
/Users/mannan/Desktop/SpringBoot/Java-Kafka/start-consumer.sh
```

**⏰ Wait for each service to fully start before starting the next one!**

---

## ⚙️ Prerequisites

```bash
# Install Java 17 if not already installed
brew install --cask temurin@17

# Set Maven path (if not in PATH)
export PATH=/opt/maven/bin:$PATH

# Set Java 17 as the active version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Verify Java 17
java -version  # Should show "OpenJDK 17.x.x"

# Verify Maven
mvn -version

# Verify Docker is running
docker ps
```

---

## 📦 **Step 1: Start Docker Services (Do this ONCE)**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
docker compose up -d
sleep 30  # Wait for Kafka to fully initialize
```

**Verify:**
```bash
docker compose ps  # Should show 3 containers: zookeeper, kafka, kafka-ui
```

---

## 🏗️ **Step 2: Build All Modules (Do this ONCE or after code changes)**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
export PATH=/opt/maven/bin:$PATH
mvn clean install
```

**Expected:** "BUILD SUCCESS" with all 4 modules compiled successfully.

---

## 🔥 **Step 3: Start Services (3 Terminals)**

### **Terminal 1: File Processor (START THIS FIRST)**

**⚠️ IMPORTANT: You MUST use Java 17 (not Java 25 or any other version)**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/file-processor-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Verify Java 17 is active
java -version  # Should show "OpenJDK 17.x.x"

# Now start the application
mvn spring-boot:run
```

**✅ Wait for these messages:**
```
INFO  : Started FileProcessorApplication in X seconds
INFO  : Tomcat started on port 8081 (http)
INFO  : H2 TCP Server started on port 9093
```

**⏰ DO NOT START TERMINAL 2 UNTIL YOU SEE "Started FileProcessorApplication"**

---

### **Terminal 2: Kafka Publisher (START AFTER TERMINAL 1)**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-publisher-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

**✅ Wait for these messages:**
```
INFO  : Started KafkaPublisherApplication in X seconds
INFO  : Tomcat started on port 8082 (http)
INFO  : Found 0 pending outbox events to publish  ← Should repeat every 5 seconds
```

---

### **Terminal 3: Kafka Consumer**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-consumer-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

**✅ Wait for these messages:**
```
INFO  : Started KafkaConsumerApplication in X seconds
INFO  : Tomcat started on port 8083 (http)
INFO  : partitions assigned: [transaction-events-0]  ← This means it's connected!
```

---

## ✅ **Step 4: Verify Everything is Running**

```bash
# Check all ports
nc -zv localhost 8081  # File Processor
nc -zv localhost 8082  # Publisher
nc -zv localhost 8083  # Consumer
nc -zv localhost 9092  # Kafka
nc -zv localhost 8080  # Kafka UI
nc -zv localhost 9093  # H2 TCP Server
```

**All should show "succeeded" or "open"**

---

## 🎯 **Step 5: Trigger File Processing**

```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Expected response:**
```
File processing job started successfully
```

---

## 📊 **Step 6: Verify Results**

### **Check Logs in Each Terminal:**

**Terminal 1 (File Processor):**
```
INFO  : Processed header: FileHeader(...)
INFO  : Saved transaction: 1
INFO  : Created outbox event: 1
...
```

**Terminal 2 (Publisher) - Wait 5 seconds:**
```
INFO  : Found 10 pending outbox events to publish
INFO  : Publishing event 1 to Kafka topic: transaction-events
INFO  : Successfully published event 1 to partition 0 with offset 0
...
```

**Terminal 3 (Consumer):**
```
INFO  : Received message from partition 0 with offset 0
INFO  : Processing Transaction Event:
INFO  :   Transaction ID: 1
...
```

---

### **Check H2 Database:**

**Open:** http://localhost:8081/h2-console

**JDBC URL:** `jdbc:h2:mem:fileprocessordb`
**Username:** `sa`
**Password:** (empty)

**Run queries:**
```sql
-- See all transactions
SELECT * FROM TRANSACTIONS;

-- See outbox events with processed timestamp
SELECT ID, AGGREGATE_ID, EVENT_TYPE, CREATED_AT, PROCESSED_AT 
FROM OUTBOX_EVENTS 
ORDER BY ID;

-- Count pending events (should be 0)
SELECT COUNT(*) FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NULL;
```

---

### **Check Kafka UI:**

**Open:** http://localhost:8080

1. Click "Topics" → "transaction-events"
2. Click "Messages" tab
3. You should see 10 messages!

---

## 🛑 **Stopping Everything**

```bash
# In each terminal (1, 2, 3), press:
Ctrl + C

# Then stop Docker:
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka
docker compose down
```
    
---

## 🔄 **Process File Again (Repeat Step 5)**

```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

**Result:** 10 more transactions, 20 total messages in Kafka!

---

## 🐛 **Troubleshooting**

### **Issue: "Connection refused: localhost:9093"**

**Solution:** You started Terminal 2 (Publisher) BEFORE Terminal 1 (File Processor) was fully ready.

**Fix:**
1. Stop Terminal 2 (Ctrl+C)
2. Restart Terminal 2 AFTER seeing "Started FileProcessorApplication" in Terminal 1

---

### **Issue: "Port 8081 already in use"**

**Solution:**
```bash
lsof -i :8081
kill -9 <PID>
```

---

### **Issue: Consumer not receiving messages**

**Check:**
1. Kafka is running: `docker ps` should show kafka container
2. Publisher logs show "Successfully published"
3. Consumer logs show "partitions assigned"
4. Check Kafka UI for messages

---

### **Issue: "mvn: command not found"**

**Solution:**
```bash
export PATH=/opt/maven/bin:$PATH
```

---

## 📝 **Architecture Summary**

```
┌─────────────────────┐
│  File Processor     │ Port 8081
│  (Terminal 1)       │ 
│  - Embedded H2      │ jdbc:h2:mem:fileprocessordb
│  - H2 TCP Server    │ Port 9093
└──────────┬──────────┘
           │
           │ (publishes via TCP)
           ▼
┌─────────────────────┐
│  Kafka Publisher    │ Port 8082
│  (Terminal 2)       │
│  Connects to H2     │ jdbc:h2:tcp://localhost:9093/mem:fileprocessordb
└──────────┬──────────┘
           │
           │ (publishes events)
           ▼
┌─────────────────────┐
│  Kafka Broker       │ Port 9092
└──────────┬──────────┘
           │
           │ (consumes events)
           ▼
┌─────────────────────┐
│  Kafka Consumer     │ Port 8083
│  (Terminal 3)       │
└─────────────────────┘
```

---

## ✅ **Success Checklist**

- [ ] Docker containers running (kafka, zookeeper, kafka-ui)
- [ ] File Processor started (port 8081, H2 TCP on 9093)
- [ ] Publisher started (port 8082, polling every 5s)
- [ ] Consumer started (port 8083, partitions assigned)
- [ ] File processed via curl command
- [ ] Database shows 10 transactions
- [ ] Database shows 10 outbox events with PROCESSED_AT
- [ ] Kafka UI shows 10 messages
- [ ] Consumer logs show messages processed

---

**🎉 You're all set! Happy Kafka learning!**

