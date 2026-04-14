# Quick Start Guide

## 🚀 Quick Start (Automated)

### Option 1: Using the Startup Script
```bash
./start.sh
```

This will:
1. Check prerequisites (Java, Maven, Docker)
2. Start Kafka infrastructure with Docker Compose
3. Build all modules
4. Display instructions for starting services

### Option 2: Manual Setup

#### Step 1: Start Kafka
```bash
docker-compose up -d
```

#### Step 2: Build Project
```bash
mvn clean install
```

#### Step 3: Start Services (3 separate terminals)

**Terminal 1:**
```bash
cd file-processor-service
mvn spring-boot:run
```

**Terminal 2:**
```bash
cd kafka-publisher-service
mvn spring-boot:run
```

**Terminal 3:**
```bash
cd kafka-consumer-service
mvn spring-boot:run
```

#### Step 4: Test the Application
```bash
./test.sh
```

Or manually:
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

## 📊 Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| File Processor API | http://localhost:8081 | Process transaction files |
| Kafka Publisher | http://localhost:8082 | Publish events to Kafka |
| Kafka Consumer | http://localhost:8083 | Consume Kafka events |
| Kafka UI | http://localhost:8080 | Monitor Kafka topics |
| H2 Console (Processor) | http://localhost:8081/h2-console | View transactions DB |
| H2 Console (Publisher) | http://localhost:8082/h2-console | View outbox DB |

## 🗄️ Database Connection Details

### File Processor DB
- **JDBC URL:** `jdbc:h2:mem:fileprocessordb`
- **Username:** `sa`
- **Password:** _(leave empty)_

### Publisher DB
- **JDBC URL:** `jdbc:h2:mem:publisherdb`
- **Username:** `sa`
- **Password:** _(leave empty)_

## 🧪 Quick Test Queries

### Check Transactions
```sql
SELECT * FROM TRANSACTIONS;
```

### Check Outbox Events
```sql
SELECT * FROM OUTBOX_EVENTS;
```

### Check Pending Events
```sql
SELECT * FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NULL;
```

### Check Processed Events
```sql
SELECT * FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NOT NULL;
```

## 📝 Sample Transaction File

Location: `input/transactions.txt`

Format:
```
HDR,BATCH001,2026-04-13,CORE_SYSTEM
TXN,PURCHASE,1234567890123456,ACC001,150.75
TXN,WITHDRAWAL,9876543210987654,ACC002,500.00
TRL,2,650.75
```

## 🛑 Stopping the Application

```bash
./stop.sh
```

Or manually:
```bash
# Stop Docker containers
docker-compose down

# Stop Spring Boot services by pressing Ctrl+C in each terminal
```

## 🔍 Monitoring & Debugging

### Check Kafka Topics
Visit: http://localhost:8080
- Navigate to Topics → `transaction-events`
- View messages, partitions, and consumer groups

### View Logs

**File Processor Logs:**
Look for:
- "Saved transaction: {id}"
- "Created outbox event: {id}"

**Publisher Logs:**
Look for:
- "Found {N} pending outbox events to publish"
- "Successfully published event {id}"

**Consumer Logs:**
Look for:
- "Received message from partition"
- "Successfully processed and acknowledged transaction event"

## 🎯 What to Expect

1. **File Processing:** Transactions from `input/transactions.txt` are parsed and saved
2. **Dual Write:** Both TRANSACTION and OUTBOX_EVENTS tables are populated
3. **Kafka Publishing:** Outbox events are polled every 5 seconds and published to Kafka
4. **Event Consumption:** Consumer receives events and processes them with manual acknowledgment

## 📈 Architecture Flow

```
File (transactions.txt)
    ↓
File Processor (Port 8081)
    ↓
Database (H2)
    ├── TRANSACTIONS table
    └── OUTBOX_EVENTS table
         ↓
Kafka Publisher (Port 8082) - Polls every 5s
         ↓
Kafka Topic (transaction-events)
         ↓
Kafka Consumer (Port 8083) - Manual Ack
         ↓
Business Logic Processing
```

## 💡 Tips

1. **Start services in order:** File Processor → Publisher → Consumer
2. **Wait for startup:** Each service takes ~30 seconds to start
3. **Check logs:** Monitor all three terminals for real-time activity
4. **Kafka UI:** Use the web interface to visualize message flow
5. **H2 Console:** Query databases to see stored data

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8081
lsof -i :8082
lsof-i :8083
lsof -i :9092

# Kill the process
kill -9 <PID>
```

### Kafka Not Starting
```bash
# Check Docker containers
docker ps

# View logs
docker-compose logs kafka
docker-compose logs zookeeper

# Restart
docker-compose restart
```

### Build Failures
```bash
# Clean and rebuild
mvn clean install -U
```

### Database Issues
```bash
# Services use in-memory H2, so restart the service to reset
# Press Ctrl+C and run mvn spring-boot:run again
```

## 📚 Learn More

Check the main [README.md](README.md) for:
- Detailed architecture explanation
- Outbox pattern benefits
- Configuration options
- API documentation
- Testing strategies

