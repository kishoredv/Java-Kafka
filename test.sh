#!/bin/bash

echo "🧪 Testing Java-Kafka Application"
echo "=================================="
echo ""

# Check if services are running
echo "1. Checking if File Processor Service is running..."
if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1 || nc -z localhost 8081 2>/dev/null; then
    echo "   ✅ File Processor Service is running (Port 8081)"
else
    echo "   ❌ File Processor Service is NOT running"
fi

echo ""
echo "2. Checking if Kafka Publisher Service is running..."
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1 || nc -z localhost 8082 2>/dev/null; then
    echo "   ✅ Kafka Publisher Service is running (Port 8082)"
else
    echo "   ❌ Kafka Publisher Service is NOT running"
fi

echo ""
echo "3. Checking if Kafka Consumer Service is running..."
if nc -z localhost 8083 2>/dev/null; then
    echo "   ✅ Kafka Consumer Service is running (Port 8083)"
else
    echo "   ❌ Kafka Consumer Service is NOT running"
fi

echo ""
echo "4. Checking Kafka..."
if nc -z localhost 9092 2>/dev/null; then
    echo "   ✅ Kafka is running (Port 9092)"
else
    echo "   ❌ Kafka is NOT running"
fi

echo ""
echo "=================================="
echo "🚀 Triggering File Processing..."
echo "=================================="
echo ""

response=$(curl -s -X POST http://localhost:8081/api/file-processor/process)
echo "Response: $response"

echo ""
echo "=================================="
echo "📊 Check the logs in your terminals to see:"
echo "   - File processing (Terminal 1)"
echo "   - Kafka publishing (Terminal 2)"
echo "   - Event consumption (Terminal 3)"
echo ""
echo "🌐 View Kafka Messages:"
echo "   http://localhost:8080"
echo ""
echo "🗄️ View Database:"
echo "   http://localhost:8081/h2-console"
echo "   JDBC URL: jdbc:h2:mem:fileprocessordb"
echo "   Username: sa"
echo "   Password: (leave empty)"
echo "=================================="

