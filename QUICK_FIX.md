# 🚨 Quick Fix for "Port Already in Use" Error

## Problem
When you see this error:
```
Web server failed to start. Port 8081 was already in use.
```

## Solution

### **Step 1: Kill the Process Using Port 8081**

```bash
lsof -ti :8081 | xargs kill -9
```

### **Step 2: Verify Port is Free**

```bash
lsof -i :8081
```

**Expected:** No output (port is free)

### **Step 3: Start File Processor Again**

```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/file-processor-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

---

## ✅ Verify Everything is Running

After starting the file-processor:

```bash
# Check if file-processor is running (port 8081)
nc -zv localhost 8081

# Check if H2 TCP Server is running (port 9093)
nc -zv localhost 9093
```

**Both should show "succeeded"**

---

## 🎯 Next Steps

Once you see in Terminal 1:
```
✅ H2 TCP Server started successfully on port 9093
Started FileProcessorApplication in X seconds
Tomcat started on port 8081 (http)
```

**Then start Terminal 2 (Publisher):**
```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-publisher-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

**Then start Terminal 3 (Consumer):**
```bash
cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-consumer-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

---

## 🔄 If You Need to Restart Everything

**Kill all Java processes:**
```bash
pkill -9 java
```

**Or kill specific ports:**
```bash
# File Processor
lsof -ti :8081 | xargs kill -9

# Publisher
lsof -ti :8082 | xargs kill -9

# Consumer
lsof -ti :8083 | xargs kill -9

# H2 TCP Server
lsof -ti :9093 | xargs kill -9
```

**Then start fresh from Terminal 1!**

---

## 💡 Pro Tip

Always check what's running before starting services:
```bash
lsof -i :8081  # File Processor
lsof -i :8082  # Publisher
lsof -i :8083  # Consumer
lsof -i :9093  # H2 TCP Server
```

If you see any output, those ports are in use. Kill them first!

