# 🚀 Quick Reference Card

## ⚡ Quick Commands

### Setup & Start
```bash
./verify-setup.sh                              # Check prerequisites
./start.sh                                     # Automated setup
```

### Manual Start (3 Terminals)
```bash
# Terminal 1
cd file-processor-service && mvn spring-boot:run

# Terminal 2  
cd kafka-publisher-service && mvn spring-boot:run

# Terminal 3
cd kafka-consumer-service && mvn spring-boot:run
```

### Trigger Processing
```bash
curl -X POST http://localhost:8081/api/file-processor/process
```

### Stop
```bash
./stop.sh                                      # Stop Docker
Ctrl+C in each terminal                        # Stop services
```

---

## 🌐 Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| File Processor API | http://localhost:8081 | - |
| Kafka UI | http://localhost:8080 | - |
| H2 Console (Processor) | http://localhost:8081/h2-console | jdbc:h2:mem:fileprocessordb / sa / (empty) |
| H2 Console (Publisher) | http://localhost:8082/h2-console | jdbc:h2:mem:publisherdb / sa / (empty) |

---

## 🗄️ Quick SQL Queries

### View All Transactions
```sql
SELECT * FROM TRANSACTIONS;
```

### View Pending Outbox Events
```sql
SELECT * FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NULL;
```

### View Processed Events
```sql
SELECT * FROM OUTBOX_EVENTS WHERE PROCESSED_AT IS NOT NULL;
```

### Count Events by Status
```sql
SELECT 
    CASE WHEN PROCESSED_AT IS NULL THEN 'Pending' ELSE 'Processed' END AS status,
    COUNT(*) AS count
FROM OUTBOX_EVENTS
GROUP BY CASE WHEN PROCESSED_AT IS NULL THEN 'Pending' ELSE 'Processed' END;
```

---

## 📁 File Locations

| Type | Location |
|------|----------|
| Transaction File | `input/transactions.txt` |
| Logs | Terminal output (each service) |
| Config Files | `*/src/main/resources/application.yml` |
| Source Code | `*/src/main/java/com/example/kafka/` |

---

## 📝 File Format Reference

### Transaction File Structure
```
HDR,<batchId>,<date>,<system>
TXN,<type>,<cardNumber>,<accountNumber>,<amount>
TXN,<type>,<cardNumber>,<accountNumber>,<amount>
...
TRL,<count>,<totalAmount>
```

### Example
```
HDR,BATCH001,2026-04-13,CORE_SYSTEM
TXN,PURCHASE,1234567890123456,ACC001,150.75
TXN,WITHDRAWAL,9876543210987654,ACC002,500.00
TRL,2,650.75
```

---

## 🔍 Monitoring Checklist

### After Processing, Verify:

- [ ] **Terminal 1:** "Saved transaction" logs
- [ ] **Terminal 1:** "Created outbox event" logs
- [ ] **Terminal 2:** "Successfully published event" logs
- [ ] **Terminal 3:** "Successfully processed and acknowledged" logs
- [ ] **H2 Console:** TRANSACTIONS table populated
- [ ] **H2 Console:** OUTBOX_EVENTS have PROCESSED_AT timestamps
- [ ] **Kafka UI:** Messages visible in transaction-events topic
- [ ] **Kafka UI:** Consumer lag = 0

---

## 🐛 Quick Troubleshooting

### Port Already in Use
```bash
lsof -i :8081              # Find process
kill -9 <PID>              # Kill process
```

### Kafka Not Starting
```bash
docker-compose restart kafka
sleep 30                   # Wait for startup
```

### Services Not Connecting
```bash
docker-compose ps          # Check containers
docker-compose logs kafka  # View Kafka logs
```

### Reset Everything
```bash
./stop.sh
docker-compose down -v     # Remove volumes
./start.sh                 # Fresh start
```

---

## 📊 Key Metrics

| Metric | Typical Value |
|--------|--------------|
| File Processing Time | 100-500ms (10 transactions) |
| Kafka Publish Latency | 50-100ms per message |
| Consumer Processing | 100-200ms per message |
| End-to-End Latency | 5-10 seconds (with 5s polling) |
| Outbox Polling Interval | 5 seconds (configurable) |

---

## ⚙️ Configuration Shortcuts

### Change Polling Interval
```yaml
# kafka-publisher-service/src/main/resources/application.yml
outbox:
  polling:
    interval: 5000  # milliseconds
```

### Change File Path
```yaml
# file-processor-service/src/main/resources/application.yml
file:
  input:
    path: input/transactions.txt
```

### Change Ports
```yaml
# */src/main/resources/application.yml
server:
  port: 8081  # Change as needed
```

---

## 📖 Documentation Quick Links

| Document | Purpose |
|----------|---------|
| README.md | Complete project overview |
| QUICKSTART.md | Fast setup guide |
| ARCHITECTURE.md | Design & diagrams |
| EXAMPLE_WALKTHROUGH.md | Step-by-step tutorial |
| PROJECT_SUMMARY.md | Documentation index |

---

## 🎯 Success Indicators

### ✅ Everything Working If You See:

**File Processor:**
- "Saved transaction: {id}"
- "Created outbox event: {id}"

**Publisher:**
- "Found {N} pending outbox events"
- "Successfully published event {id}"

**Consumer:**
- "Received message from partition"
- "Successfully processed and acknowledged"

**Database:**
- TRANSACTIONS table has records
- OUTBOX_EVENTS have PROCESSED_AT

**Kafka UI:**
- Topic "transaction-events" exists
- Messages visible
- Consumer lag = 0

---

## 🛠️ Emergency Commands

### Force Stop Everything
```bash
pkill -f "spring-boot"
docker-compose down -v
```

### Clean Build
```bash
mvn clean install -DskipTests
```

### Reset Database
```bash
# Just restart the service (H2 is in-memory)
# Press Ctrl+C and run mvn spring-boot:run again
```

---

## 📞 Quick Help

**Issue:** Can't find a command  
**Solution:** Check PATH or use absolute paths

**Issue:** Maven/Docker Compose not found  
**Solution:** Install or use `docker compose` (without hyphen)

**Issue:** Ports busy  
**Solution:** Kill processes or change ports in application.yml

**Issue:** Kafka errors  
**Solution:** Wait 30s after startup, check docker-compose logs

---

**Keep this card handy for quick reference while working with the application!**

**📁 Save this as:** `QUICK_REFERENCE.md`

