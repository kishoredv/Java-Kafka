# 🎯 Visual Execution Flow

This document provides a visual representation of how to execute the application step-by-step.

```
┌─────────────────────────────────────────────────────────────────┐
│                   PART 1: PREREQUISITES CHECK                    │
└─────────────────────────────────────────────────────────────────┘

Step 1: ☑️ Check Java 17+          → java -version
Step 2: ☑️ Check Maven 3.6+         → mvn -version
Step 3: ☑️ Check Docker installed   → docker --version
Step 4: ☑️ Check Docker Compose     → docker-compose --version

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                 PART 2: START INFRASTRUCTURE                     │
└─────────────────────────────────────────────────────────────────┘

Step 5: 📁 Navigate to project     → cd Java-Kafka/
Step 6: 🐳 Start Docker services   → docker-compose up -d
Step 7: ✅ Verify containers        → docker-compose ps
Step 8: ⏳ Wait for Kafka          → sleep 30
Step 9: 🌐 Check Kafka UI          → http://localhost:8080

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                   PART 3: BUILD APPLICATION                      │
└─────────────────────────────────────────────────────────────────┘

Step 10: 🔨 Build all modules      → mvn clean install

         (Wait 2-5 minutes for dependencies to download)

         Expected: "BUILD SUCCESS"

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                  PART 4: START SERVICES (3 Terminals)            │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   TERMINAL 1         │  │   TERMINAL 2         │  │   TERMINAL 3         │
│   Port 8081          │  │   Port 8082          │  │   Port 8083          │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘

Step 11: Start File      Step 13: Start Kafka     Step 14: Start Kafka
         Processor               Publisher                Consumer

cd file-processor-       cd kafka-publisher-      cd kafka-consumer-
service                  service                  service

mvn spring-boot:run      mvn spring-boot:run      mvn spring-boot:run

Wait for:                Wait for:                Wait for:
"Tomcat started          "Tomcat started          "partitions assigned"
 on port 8081"            on port 8082"

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                 PART 5: VERIFY ALL RUNNING                       │
└─────────────────────────────────────────────────────────────────┘

Step 15: ✅ Check all ports open

         nc -zv localhost 8081  ✓
         nc -zv localhost 8082  ✓
         nc -zv localhost 8083  ✓
         nc -zv localhost 9092  ✓

Step 16: 🌐 Open web interfaces

         http://localhost:8080        (Kafka UI)
         http://localhost:8081/h2-console  (Database)

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                PART 6: PROCESS TRANSACTIONS                      │
└─────────────────────────────────────────────────────────────────┘

Step 17: 📄 Verify file exists
         cat input/transactions.txt

Step 18: 🚀 Trigger processing
         curl -X POST http://localhost:8081/api/file-processor/process

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                   WATCH THE MAGIC HAPPEN! ✨                     │
└─────────────────────────────────────────────────────────────────┘

Step 19: 👀 Watch logs in each terminal

┌───────────────────────┐
│ TERMINAL 1            │  ──▶  File parsed
│ File Processor        │  ──▶  Transactions saved
└───────────────────────┘  ──▶  Outbox events created
         │
         ▼ (Data saved to H2 database)
         │
┌───────────────────────┐
│ DATABASE              │
│ ┌─────────────────┐   │
│ │ TRANSACTIONS    │   │  ◀── Transaction records
│ └─────────────────┘   │
│ ┌─────────────────┐   │
│ │ OUTBOX_EVENTS   │   │  ◀── Event records
│ └─────────────────┘   │
└───────────────────────┘
         │
         ▼ (Polling every 5 seconds)
         │
┌───────────────────────┐
│ TERMINAL 2            │  ──▶  Found pending events
│ Kafka Publisher       │  ──▶  Publishing to Kafka
└───────────────────────┘  ──▶  Successfully published
         │
         ▼ (Events sent to Kafka)
         │
┌───────────────────────┐
│ KAFKA BROKER          │
│ Topic:                │
│ transaction-events    │  ◀── Messages stored
└───────────────────────┘
         │
         ▼ (Consumer receives)
         │
┌───────────────────────┐
│ TERMINAL 3            │  ──▶  Message received
│ Kafka Consumer        │  ──▶  Processing event
└───────────────────────┘  ──▶  Acknowledged ✓

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                  PART 7: VERIFY DATA                             │
└─────────────────────────────────────────────────────────────────┘

Step 20: 🗄️ Check database
         http://localhost:8081/h2-console
         
         SELECT * FROM TRANSACTIONS;
         → Should show 10 transactions

Step 21: 📊 Check outbox events
         SELECT * FROM OUTBOX_EVENTS;
         → All should have PROCESSED_AT timestamp

Step 22: ✅ Check pending (should be 0)
         SELECT COUNT(*) FROM OUTBOX_EVENTS 
         WHERE PROCESSED_AT IS NULL;
         → Result: 0

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                PART 8: VERIFY IN KAFKA UI                        │
└─────────────────────────────────────────────────────────────────┘

Step 23: 📱 View messages
         http://localhost:8080 → Topics → transaction-events
         → Should see 10 messages

Step 24: 👥 Check consumer group
         Consumers → transaction-consumer-group
         → Lag should be 0 (all consumed)

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                   🎉 SUCCESS! ALL WORKING! 🎉                    │
└─────────────────────────────────────────────────────────────────┘

✅ File processed
✅ Transactions saved to database
✅ Outbox events created
✅ Events published to Kafka
✅ Consumer processed all events
✅ No pending events
✅ No consumer lag

                            ↓

┌─────────────────────────────────────────────────────────────────┐
│                  PART 9: CLEANUP (When Done)                     │
└─────────────────────────────────────────────────────────────────┘

Step 26: 🛑 Stop services
         Press Ctrl+C in Terminal 1, 2, 3

Step 27: 🐳 Stop Docker
         docker-compose down

Step 28: ✅ Verify stopped
         docker-compose ps
         → Should be empty

═══════════════════════════════════════════════════════════════════
                         THE END
═══════════════════════════════════════════════════════════════════
```

---

## 🕐 Estimated Time for Each Part

| Part | Description | Time |
|------|-------------|------|
| Part 1 | Prerequisites Check | 2-5 minutes |
| Part 2 | Start Infrastructure | 1-2 minutes |
| Part 3 | Build Application | 2-5 minutes (first time) |
| Part 4 | Start Services | 3-5 minutes |
| Part 5 | Verify All Running | 2-3 minutes |
| Part 6 | Process Transactions | 1 minute |
| Part 7 | Verify Data | 2-3 minutes |
| Part 8 | Verify Kafka UI | 2-3 minutes |
| Part 9 | Cleanup | 1 minute |
| **TOTAL** | **Complete Execution** | **16-30 minutes** |

---

## 🎯 Quick Visual Status Check

After starting everything, your system should look like this:

```
┌─────────────────────────────────────────────────────────────────┐
│                      SYSTEM STATUS                               │
└─────────────────────────────────────────────────────────────────┘

Docker Containers:
  [✓] zookeeper     : Up    (Port 2181)
  [✓] kafka         : Up    (Port 9092)
  [✓] kafka-ui      : Up    (Port 8080)

Spring Boot Services:
  [✓] file-processor: Up    (Port 8081)
  [✓] kafka-publisher: Up   (Port 8082)
  [✓] kafka-consumer: Up    (Port 8083)

Web Interfaces:
  [✓] Kafka UI      : http://localhost:8080
  [✓] H2 Console    : http://localhost:8081/h2-console

Data Flow Status:
  [✓] File Parser   : Ready to accept requests
  [✓] Database      : Tables created and ready
  [✓] Kafka Topic   : Created (transaction-events)
  [✓] Publisher     : Polling every 5 seconds
  [✓] Consumer      : Listening to topic

═══════════════════════════════════════════════════════════════════
Status: ✅ ALL SYSTEMS GO!
═══════════════════════════════════════════════════════════════════
```

---

## 🎬 Terminal Layout Suggestion

For best experience, arrange your terminals like this:

```
┌─────────────────────────────┬─────────────────────────────┐
│                             │                             │
│  TERMINAL 1 (Top Left)      │  TERMINAL 2 (Top Right)     │
│  File Processor (8081)      │  Kafka Publisher (8082)     │
│  cd file-processor-service  │  cd kafka-publisher-service │
│  mvn spring-boot:run        │  mvn spring-boot:run        │
│                             │                             │
├─────────────────────────────┼─────────────────────────────┤
│                             │                             │
│  TERMINAL 3 (Bottom Left)   │  TERMINAL 4 (Bottom Right)  │
│  Kafka Consumer (8083)      │  Commands & Testing         │
│  cd kafka-consumer-service  │  curl commands              │
│  mvn spring-boot:run        │  docker-compose commands    │
│                             │                             │
└─────────────────────────────┴─────────────────────────────┘
```

---

## 📋 Pre-Flight Checklist

Before starting, make sure:

- [ ] Docker Desktop is **running** (check system tray icon)
- [ ] No other applications using ports 8080-8083, 9092, 2181
- [ ] At least 4GB RAM available
- [ ] At least 2GB disk space available
- [ ] Internet connection available (for Maven dependencies)
- [ ] Java 17+ installed and in PATH
- [ ] Maven 3.6+ installed and in PATH

---

## 🆘 Quick Troubleshooting Decision Tree

```
Problem occurred?
    │
    ├─ Services won't start?
    │   │
    │   ├─ Port already in use?
    │   │   └─▶ Use lsof -i :<port> and kill process
    │   │
    │   ├─ Build failed?
    │   │   └─▶ Check Java version (must be 17+)
    │   │
    │   └─ Can't connect to Kafka?
    │       └─▶ Wait 30s, check docker-compose logs kafka
    │
    ├─ No logs appearing?
    │   │
    │   └─▶ Wrong directory? Check pwd
    │       Restart service with Ctrl+C and try again
    │
    ├─ Database empty?
    │   │
    │   ├─ Wrong JDBC URL?
    │   │   └─▶ Use exact: jdbc:h2:mem:fileprocessordb
    │   │
    │   └─ Processing not triggered?
    │       └─▶ Run: curl -X POST http://localhost:8081/api/file-processor/process
    │
    └─ No messages in Kafka?
        │
        ├─ Publisher not running?
        │   └─▶ Check Terminal 2, restart if needed
        │
        └─ Wait 5 seconds (polling interval)
            └─▶ Check Kafka UI again
```

---

**For complete details, see:** `LOCAL_EXECUTION_GUIDE.md`

**For quick commands, see:** `QUICK_REFERENCE.md`

