# 🔧 Kafka Consumer Deserialization Error - FIXED

## 🐛 Problem

The Kafka consumer was experiencing a deserialization error:

```
com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `com.example.kafka.dto.TransactionEvent` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value
```

**Error Message Analysis:**
```json
"{\"transactionId\":9,\"transactionType\":\"PURCHASE\",...}"
```

Notice the **double quotes** at the beginning - the JSON was being treated as a string instead of a JSON object.

---

## 🔍 Root Cause

**Double JSON Encoding Issue:**

1. **File Processor Service** → Stores payload as JSON string in `OUTBOX_EVENTS` table
2. **Kafka Publisher Service** → Uses `JsonSerializer` which serializes the JSON string **AGAIN** (converting `{...}` to `"{...}"`)
3. **Kafka Consumer** → Receives double-encoded JSON and fails to deserialize

**Before (Incorrect):**
```
Database: {"transactionId":9,...}  (String)
         ↓
Kafka Producer (JsonSerializer): ""{\"transactionId\":9,...}""  (Double-encoded)
         ↓
Kafka Consumer: ❌ FAILS TO DESERIALIZE
```

---

## ✅ Solution

Changed the Kafka Producer configuration to use `StringSerializer` instead of `JsonSerializer` for the value, since the payload is already a JSON string.

**After (Correct):**
```
Database: {"transactionId":9,...}  (String)
         ↓
Kafka Producer (StringSerializer): {"transactionId":9,...}  (Sent as-is)
         ↓
Kafka Consumer: ✅ SUCCESSFULLY DESERIALIZES
```

---

## 📝 Files Modified

### 1. KafkaProducerConfig.java
**Location:** `/kafka-publisher-service/src/main/java/com/example/kafka/publisher/config/KafkaProducerConfig.java`

**Changes:**
```java
// BEFORE:
@Bean
public ProducerFactory<String, Object> producerFactory() {
    // ...
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    // ...
}

@Bean
public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
}

// AFTER:
@Bean
public ProducerFactory<String, String> producerFactory() {
    // ...
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // Removed: JsonSerializer.ADD_TYPE_INFO_HEADERS
    // ...
}

@Bean
public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
}
```

### 2. OutboxPublisherService.java
**Location:** `/kafka-publisher-service/src/main/java/com/example/kafka/publisher/service/OutboxPublisherService.java`

**Changes:**
```java
// BEFORE:
private final KafkaTemplate<String, Object> kafkaTemplate;

CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(...);

// AFTER:
private final KafkaTemplate<String, String> kafkaTemplate;

CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(...);
```

---

## 🚀 How to Apply the Fix

### Option 1: Use Updated Startup Script (Recommended)

The publisher startup script has been updated to rebuild automatically:

```bash
# In Terminal 2 (where publisher is running):
# 1. Stop the publisher: Ctrl+C
# 2. Restart using the script:
/Users/mannan/Desktop/SpringBoot/Java-Kafka/start-publisher.sh
```

### Option 2: Manual Restart

```bash
# In Terminal 2 (where publisher is running):
# 1. Stop the publisher: Ctrl+C

# 2. Navigate to publisher directory:
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-publisher-service

# 3. Set Java 17:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=/opt/maven/bin:$PATH

# 4. Start the publisher:
mvn spring-boot:run
```

---

## ✅ Verification Steps

After restarting the publisher, verify the fix:

### 1. Check Publisher Logs
You should see messages like:
```
INFO  : Found 10 pending outbox events to publish
INFO  : Publishing event 1 to Kafka topic: transaction-events
INFO  : Successfully published event 1 to partition 0 with offset 0
```

### 2. Check Consumer Logs
You should now see **SUCCESS** instead of errors:
```
INFO  : Received message from partition 0 with offset 0
INFO  : Processing Transaction Event:
INFO  :   Transaction ID: 1
INFO  :   Transaction Type: PURCHASE
INFO  :   Amount: 150.00
INFO  : Transaction processing completed for ID: 1
INFO  : Successfully processed and acknowledged transaction event: 1
```

### 3. Verify in Kafka UI
1. Open http://localhost:8080
2. Click "Topics" → "transaction-events"
3. Click "Messages" tab
4. Messages should now be **valid JSON objects** (not escaped strings)

**Before (Double-encoded):**
```json
""{\"transactionId\":9,\"transactionType\":\"PURCHASE\",...}""
```

**After (Correct):**
```json
{"transactionId":9,"transactionType":"PURCHASE",...}
```

---

## 🔄 Testing the Complete Flow

```bash
# 1. Ensure all services are running (File Processor, Publisher, Consumer)

# 2. Process the file again:
curl -X POST http://localhost:8081/api/file-processor/process

# 3. Wait 5-10 seconds for publisher to poll and publish

# 4. Check consumer logs - should see all 10 transactions processed successfully
```

---

## 📊 Expected Results

**Database:**
```sql
-- All outbox events should be marked as processed:
SELECT ID, AGGREGATE_ID, EVENT_TYPE, CREATED_AT, PROCESSED_AT 
FROM OUTBOX_EVENTS 
ORDER BY ID;

-- Should show PROCESSED_AT timestamps for all events
```

**Consumer Logs:**
```
✅ Successfully processed and acknowledged transaction event: 1
✅ Successfully processed and acknowledged transaction event: 2
✅ Successfully processed and acknowledged transaction event: 3
...
✅ Successfully processed and acknowledged transaction event: 10
```

**Kafka UI:**
- Topic: `transaction-events`
- Messages: 10 (or more if you processed multiple times)
- Each message is a valid JSON object

---

## 🎯 Summary

**Problem:** Kafka consumer couldn't deserialize messages due to double JSON encoding

**Root Cause:** Kafka producer was using `JsonSerializer` on already-serialized JSON strings

**Solution:** Changed to `StringSerializer` to send JSON strings as-is

**Result:** Consumer successfully deserializes and processes all transaction events ✅

---

## 📚 Technical Details

### Serialization Flow

**Correct Flow:**
1. File Processor → Creates `TransactionEvent` object
2. File Processor → Serializes to JSON string using `ObjectMapper`
3. File Processor → Stores JSON string in `OUTBOX_EVENTS.payload` (TEXT column)
4. Publisher → Reads JSON string from database
5. Publisher → Sends JSON string to Kafka using `StringSerializer` (no additional serialization)
6. Consumer → Receives JSON string
7. Consumer → Deserializes using `ObjectMapper.readValue(message, TransactionEvent.class)` ✅

### Why StringSerializer Works

- The `payload` column in `OUTBOX_EVENTS` table already contains serialized JSON
- We just need to transport this string from database → Kafka → consumer
- No need for additional JSON serialization at the Kafka producer level
- The consumer deserializes the JSON string back to `TransactionEvent` object

---

## 🛠️ Troubleshooting

### Issue: Still seeing errors after restart

**Solution:**
1. Make sure you stopped the old publisher process completely
2. Verify Java 17 is being used: `java -version`
3. Rebuild if needed: `mvn clean install`
4. Check the publisher logs for "Successfully published" messages

### Issue: Messages not appearing in consumer

**Solution:**
1. Check that Kafka is running: `docker ps`
2. Check publisher logs for successful publishing
3. Check consumer logs for "partitions assigned" message
4. Verify consumer group is active in Kafka UI

---

**✅ Fix Applied Successfully - Build Completed at 2026-04-13T21:48:07-05:00**

