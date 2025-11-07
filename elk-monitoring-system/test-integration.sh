#!/bin/bash

echo "========================================"
echo "ELK Monitoring System 통합 테스트"
echo "========================================"
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 테스트 결과 카운터
PASSED=0
FAILED=0

# 테스트 함수
test_service() {
    local name=$1
    local url=$2
    local expected=$3

    echo -n "Testing $name... "
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)

    if [ "$response" = "$expected" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $response)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected, Got: $response)"
        ((FAILED++))
        return 1
    fi
}

echo "1. Docker 서비스 상태 확인"
echo "----------------------------------------"

# MySQL 확인
docker exec elk-mysql mysql -uroot -proot1234 -e "SELECT 1" &>/dev/null
if [ $? -eq 0 ]; then
    echo -e "  MySQL: ${GREEN}✓ Running${NC}"
    ((PASSED++))
else
    echo -e "  MySQL: ${RED}✗ Not Running${NC}"
    ((FAILED++))
fi

# Elasticsearch 확인
ES_STATUS=$(curl -s http://localhost:9200/_cluster/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$ES_STATUS" = "green" ] || [ "$ES_STATUS" = "yellow" ]; then
    echo -e "  Elasticsearch: ${GREEN}✓ Running${NC} (Status: $ES_STATUS)"
    ((PASSED++))
else
    echo -e "  Elasticsearch: ${RED}✗ Not Running${NC}"
    ((FAILED++))
fi

# Logstash 확인
LOGSTASH_STATUS=$(curl -s http://localhost:9600/_node/stats | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ "$LOGSTASH_STATUS" = "green" ]; then
    echo -e "  Logstash: ${GREEN}✓ Running${NC}"
    ((PASSED++))
else
    echo -e "  Logstash: ${RED}✗ Not Running${NC}"
    ((FAILED++))
fi

# Kibana 확인
KIBANA_STATUS=$(curl -s http://localhost:5601/api/status | grep -o '"level":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ "$KIBANA_STATUS" = "available" ]; then
    echo -e "  Kibana: ${GREEN}✓ Running${NC}"
    ((PASSED++))
else
    echo -e "  Kibana: ${RED}✗ Not Running${NC}"
    ((FAILED++))
fi

echo ""
echo "2. Spring Boot 애플리케이션 테스트"
echo "----------------------------------------"

# Health Check
test_service "Health Check" "http://localhost:8080/health" "200"

# User API - Create
echo -n "Testing User Creation... "
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트유저","email":"test-'$RANDOM'@example.com","phone":"010-0000-0000"}')

if echo "$CREATE_RESPONSE" | grep -q "\"id\""; then
    USER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✓ PASSED${NC} (User ID: $USER_ID)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED++))
    USER_ID=1
fi

# User API - Get
test_service "Get User by ID" "http://localhost:8080/api/users/$USER_ID" "200"

# User API - Get All
test_service "Get All Users" "http://localhost:8080/api/users" "200"

# Error Simulation
test_service "Error Simulation" "http://localhost:8080/api/users/test/error" "500"

echo ""
echo "3. 로그 수집 검증"
echo "----------------------------------------"

echo "Waiting 3 seconds for logs to be indexed..."
sleep 3

# Elasticsearch 인덱스 확인
echo -n "Checking Elasticsearch indices... "
INDICES=$(curl -s "http://localhost:9200/_cat/indices?v" | grep application-logs)
if [ ! -z "$INDICES" ]; then
    echo -e "${GREEN}✓ PASSED${NC}"
    echo "$INDICES"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED++))
fi

# 로그 카운트 확인
echo -n "Checking log count... "
LOG_COUNT=$(curl -s "http://localhost:9200/application-logs-*/_count" | grep -o '"count":[0-9]*' | cut -d':' -f2)
if [ "$LOG_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Total logs: $LOG_COUNT)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC} (No logs found)"
    ((FAILED++))
fi

# 로그 레벨별 통계
echo ""
echo "Log Level Statistics:"
curl -s -X GET "http://localhost:9200/application-logs-*/_search?size=0" \
  -H 'Content-Type: application/json' \
  -d '{"aggs":{"levels":{"terms":{"field":"level.keyword"}}}}' | \
  grep -o '"key":"[^"]*","doc_count":[0-9]*' | \
  sed 's/"key":"\([^"]*\)","doc_count":\([0-9]*\)/  \1: \2/'

echo ""
echo "4. 비즈니스 이벤트 검증"
echo "----------------------------------------"

# 비즈니스 이벤트 확인
echo -n "Checking business events... "
EVENT_COUNT=$(curl -s -X GET "http://localhost:9200/application-logs-*/_count" \
  -H 'Content-Type: application/json' \
  -d '{"query":{"term":{"event_type.keyword":"business_event"}}}' | \
  grep -o '"count":[0-9]*' | cut -d':' -f2)

if [ "$EVENT_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Business events: $EVENT_COUNT)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC} (No business events found)"
    ((FAILED++))
fi

# 에러 로그 확인
echo -n "Checking error logs... "
ERROR_COUNT=$(curl -s -X GET "http://localhost:9200/application-logs-*/_count" \
  -H 'Content-Type: application/json' \
  -d '{"query":{"term":{"level.keyword":"ERROR"}}}' | \
  grep -o '"count":[0-9]*' | cut -d':' -f2)

if [ "$ERROR_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Error logs: $ERROR_COUNT)"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ WARNING${NC} (No error logs found)"
fi

echo ""
echo "5. MDC 필드 검증"
echo "----------------------------------------"

# requestId 필드 확인
echo -n "Checking requestId field... "
REQUESTID_COUNT=$(curl -s -X GET "http://localhost:9200/application-logs-*/_count" \
  -H 'Content-Type: application/json' \
  -d '{"query":{"exists":{"field":"requestId"}}}' | \
  grep -o '"count":[0-9]*' | cut -d':' -f2)

if [ "$REQUESTID_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Logs with requestId: $REQUESTID_COUNT)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED++))
fi

# clientIp 필드 확인
echo -n "Checking clientIp field... "
CLIENTIP_COUNT=$(curl -s -X GET "http://localhost:9200/application-logs-*/_count" \
  -H 'Content-Type: application/json' \
  -d '{"query":{"exists":{"field":"clientIp"}}}' | \
  grep -o '"count":[0-9]*' | cut -d':' -f2)

if [ "$CLIENTIP_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ PASSED${NC} (Logs with clientIp: $CLIENTIP_COUNT)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED++))
fi

echo ""
echo "========================================"
echo "테스트 결과 요약"
echo "========================================"
echo -e "Total Tests: $(($PASSED + $FAILED))"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 모든 테스트 통과!${NC}"
    echo ""
    echo "다음 단계:"
    echo "  1. Kibana 대시보드: http://localhost:5601"
    echo "  2. 로그 생성: ./generate-logs.sh"
    echo "  3. API 테스트: curl http://localhost:8080/api/users"
    exit 0
else
    echo -e "${RED}✗ 일부 테스트 실패${NC}"
    echo ""
    echo "트러블슈팅:"
    echo "  1. Docker 서비스 확인: docker-compose ps"
    echo "  2. 로그 확인: docker-compose logs [service-name]"
    echo "  3. 재시작: docker-compose restart"
    exit 1
fi
