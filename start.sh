#!/bin/bash

echo "================================================"
echo "Java-Kafka Application Startup Script"
echo "================================================"
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists java; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

if ! command_exists mvn; then
    echo "❌ Maven is not installed. Please install Maven 3.6+."
    exit 1
fi

if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose."
    exit 1
fi

echo "✅ All prerequisites are met!"
echo ""

# Start Kafka
echo "🐳 Starting Kafka infrastructure..."
docker-compose up -d

echo "⏳ Waiting for Kafka to be ready (30 seconds)..."
sleep 30

echo ""
echo "✅ Kafka infrastructure is ready!"
echo "   - Kafka Broker: localhost:9092"
echo "   - Kafka UI: http://localhost:8080"
echo ""

# Build project
echo "🔨 Building all modules..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Instructions
echo "================================================"
echo "✅ Setup Complete!"
echo "================================================"
echo ""
echo "📝 Next Steps:"
echo ""
echo "1. Open 3 separate terminals and run:"
echo ""
echo "   Terminal 1 - File Processor Service (Port 8081):"
echo "   cd file-processor-service && mvn spring-boot:run"
echo ""
echo "   Terminal 2 - Kafka Publisher Service (Port 8082):"
echo "   cd kafka-publisher-service && mvn spring-boot:run"
echo ""
echo "   Terminal 3 - Kafka Consumer Service (Port 8083):"
echo "   cd kafka-consumer-service && mvn spring-boot:run"
echo ""
echo "2. Test the application:"
echo "   curl -X POST http://localhost:8081/api/file-processor/process"
echo ""
echo "3. Access Web Interfaces:"
echo "   - Kafka UI: http://localhost:8080"
echo "   - H2 Console (File Processor): http://localhost:8081/h2-console"
echo "   - H2 Console (Publisher): http://localhost:8082/h2-console"
echo ""
echo "================================================"

