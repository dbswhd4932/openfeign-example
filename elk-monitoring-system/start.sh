#!/bin/bash

echo "================================"
echo "ELK Monitoring System Startup"
echo "================================"

# 1. Docker 컨테이너 시작
echo ""
echo "Step 1: Starting Docker containers (ELK + MySQL)..."
docker-compose up -d

echo ""
echo "Waiting for services to be ready..."
sleep 10

# 2. Elasticsearch 상태 확인
echo ""
echo "Step 2: Checking Elasticsearch health..."
curl -s "http://localhost:9200/_cluster/health?pretty" | grep status

# 3. MySQL 상태 확인
echo ""
echo "Step 3: Checking MySQL connection..."
docker exec elk-mysql mysql -uroot -proot1234 -e "SELECT 1" 2>/dev/null && echo "MySQL is ready!" || echo "MySQL is not ready yet"

# 4. Spring Boot 애플리케이션 시작
echo ""
echo "Step 4: Starting Spring Boot application..."
echo "The application will start on http://localhost:8080"
echo ""
echo "You can test the API with:"
echo "  curl http://localhost:8080/health"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

./gradlew bootRun
