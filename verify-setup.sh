#!/bin/bash

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║        Java-Kafka Application - Setup Verification            ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

success=0
failed=0

check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $2 installed: $(command -v $1)"
        ((success++))
    else
        echo -e "${RED}✗${NC} $2 not found"
        ((failed++))
    fi
}

check_version() {
    if $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $2"
        ((success++))
    else
        echo -e "${RED}✗${NC} $2"
        ((failed++))
    fi
}

check_port() {
    if nc -z localhost $1 2>/dev/null; then
        echo -e "${YELLOW}⚠${NC} Port $1 is already in use"
    else
        echo -e "${GREEN}✓${NC} Port $1 is available"
        ((success++))
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2: $(ls -lh $1 | awk '{print $5}')"
        ((success++))
    else
        echo -e "${RED}✗${NC} $2 not found"
        ((failed++))
    fi
}

check_directory() {
    if [ -d "$1" ]; then
        file_count=$(find "$1" -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
        echo -e "${GREEN}✓${NC} $2 exists (${file_count} Java files)"
        ((success++))
    else
        echo -e "${RED}✗${NC} $2 not found"
        ((failed++))
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  1. Checking Prerequisites"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "java" "Java"
check_command "mvn" "Maven"
check_command "docker" "Docker"
check_command "docker-compose" "Docker Compose"
check_command "curl" "cURL"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  2. Checking Versions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1)
    echo -e "${BLUE}→${NC} $java_version"
fi
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version 2>&1 | head -n 1)
    echo -e "${BLUE}→${NC} $mvn_version"
fi
if command -v docker &> /dev/null; then
    docker_version=$(docker --version)
    echo -e "${BLUE}→${NC} $docker_version"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  3. Checking Port Availability"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_port 8081
check_port 8082
check_port 8083
check_port 9092
check_port 2181
check_port 8080

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  4. Checking Project Structure"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "pom.xml" "Parent POM"
check_file "docker-compose.yml" "Docker Compose"
check_file "input/transactions.txt" "Sample Transaction File"
check_directory "common-models" "Common Models Module"
check_directory "file-processor-service" "File Processor Service"
check_directory "kafka-publisher-service" "Kafka Publisher Service"
check_directory "kafka-consumer-service" "Kafka Consumer Service"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  5. Checking Documentation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "README.md" "README"
check_file "QUICKSTART.md" "Quick Start Guide"
check_file "ARCHITECTURE.md" "Architecture Documentation"
check_file "EXAMPLE_WALKTHROUGH.md" "Example Walkthrough"
check_file "PROJECT_SUMMARY.md" "Project Summary"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  6. Checking Scripts"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_file "start.sh" "Start Script"
check_file "stop.sh" "Stop Script"
check_file "test.sh" "Test Script"
check_file "verify-setup.sh" "Verify Script (this file)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  7. Project Statistics"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

java_files=$(find . -name "*.java" -not -path "*/target/*" -not -path "*/.idea/*" | wc -l | tr -d ' ')
xml_files=$(find . -name "pom.xml" -not -path "*/target/*" | wc -l | tr -d ' ')
yml_files=$(find . -name "*.yml" -not -path "*/target/*" | wc -l | tr -d ' ')
md_files=$(ls *.md 2>/dev/null | wc -l | tr -d ' ')

echo -e "${BLUE}→${NC} Java Source Files: ${java_files}"
echo -e "${BLUE}→${NC} POM Files: ${xml_files}"
echo -e "${BLUE}→${NC} YAML Configuration Files: ${yml_files}"
echo -e "${BLUE}→${NC} Documentation Files: ${md_files}"
echo -e "${BLUE}→${NC} Modules: 4 (common-models, file-processor, kafka-publisher, kafka-consumer)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  8. Docker Status"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if docker ps -a --format "table {{.Names}}\t{{.Status}}" 2>/dev/null | grep -q "kafka\|zookeeper"; then
    echo -e "${YELLOW}→${NC} Docker containers found:"
    docker ps -a --format "table {{.Names}}\t{{.Status}}" | grep -E "NAME|kafka|zookeeper"
else
    echo -e "${BLUE}→${NC} No Kafka containers running (expected for first setup)"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                    VERIFICATION SUMMARY                        ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo -e "  ${GREEN}✓ Passed:${NC} ${success}"
if [ $failed -gt 0 ]; then
    echo -e "  ${RED}✗ Failed:${NC} ${failed}"
fi
echo ""

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}  ✓ All checks passed! Your setup is ready.${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "📚 Next Steps:"
    echo ""
    echo "  1. Start the application:"
    echo "     ${BLUE}./start.sh${NC}"
    echo ""
    echo "  2. Read the quick start guide:"
    echo "     ${BLUE}cat QUICKSTART.md${NC}"
    echo ""
    echo "  3. Follow the example walkthrough:"
    echo "     ${BLUE}cat EXAMPLE_WALKTHROUGH.md${NC}"
    echo ""
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}  ✗ Some checks failed. Please install missing dependencies.${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "📋 Required Tools:"
    echo "  • Java 17+ : https://adoptium.net/"
    echo "  • Maven 3.6+ : https://maven.apache.org/download.cgi"
    echo "  • Docker : https://docs.docker.com/get-docker/"
    echo "  • Docker Compose : https://docs.docker.com/compose/install/"
    echo ""
fi

echo "════════════════════════════════════════════════════════════════"
echo ""

