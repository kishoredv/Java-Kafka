#!/bin/bash

echo "🛑 Stopping Java-Kafka Application..."
echo ""

# Stop Docker containers
echo "Stopping Kafka infrastructure..."
docker-compose down

echo ""
echo "✅ All services stopped!"
echo ""
echo "To stop Spring Boot services, press Ctrl+C in each terminal."

