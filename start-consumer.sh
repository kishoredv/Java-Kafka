#!/bin/bash

echo "🚀 Starting Kafka Consumer Service..."
echo "⚠️  Setting Java 17 as active version..."

cd /Users/mannan/Desktop/SpringBoot/Java-Kafka/kafka-consumer-service
export PATH=/opt/maven/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

echo "✅ Java version:"
java -version
echo ""
echo "🏗️  Starting application with Maven..."
mvn spring-boot:run

