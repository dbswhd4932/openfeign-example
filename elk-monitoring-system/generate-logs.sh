#!/bin/bash

echo "================================"
echo "로그 생성 스크립트"
echo "================================"
echo ""

BASE_URL="http://localhost:8080"

echo "1. 사용자 생성 (성공 케이스)..."
for i in {1..5}; do
  curl -s -X POST $BASE_URL/api/users \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"사용자$i\",\"email\":\"user$i@example.com\",\"phone\":\"010-0000-000$i\"}" > /dev/null
  echo "  - 사용자 $i 생성 완료"
done

echo ""
echo "2. 사용자 조회..."
curl -s -X GET $BASE_URL/api/users > /dev/null
echo "  - 전체 사용자 조회 완료"

echo ""
echo "3. 에러 케이스 생성..."
# 존재하지 않는 사용자 조회 (404)
curl -s -X GET $BASE_URL/api/users/9999 > /dev/null
echo "  - 404 에러 생성 (사용자 없음)"

# 중복 이메일 (409)
curl -s -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"중복","email":"user1@example.com","phone":"010-1111-1111"}' > /dev/null
echo "  - 409 에러 생성 (중복 이메일)"

# 의도적 에러 발생
curl -s -X GET $BASE_URL/api/users/test/error > /dev/null
echo "  - 500 에러 생성 (시뮬레이션)"

echo ""
echo "4. 사용자 상태 변경..."
curl -s -X PATCH "$BASE_URL/api/users/1/status?status=SUSPENDED" > /dev/null
echo "  - 사용자 1 상태 변경 (SUSPENDED)"

curl -s -X PATCH "$BASE_URL/api/users/2/status?status=INACTIVE" > /dev/null
echo "  - 사용자 2 상태 변경 (INACTIVE)"

echo ""
echo "5. 사용자 수정..."
curl -s -X PUT $BASE_URL/api/users/3 \
  -H "Content-Type: application/json" \
  -d '{"name":"수정된사용자3","email":"updated3@example.com","phone":"010-9999-9999"}' > /dev/null
echo "  - 사용자 3 수정 완료"

echo ""
echo "6. 추가 조회 요청..."
for i in {1..3}; do
  curl -s -X GET $BASE_URL/api/users/$i > /dev/null
  echo "  - 사용자 $i 조회 완료"
done

echo ""
echo "================================"
echo "로그 생성 완료!"
echo "================================"
echo ""
echo "Kibana에서 확인하세요: http://localhost:5601"
echo "Elasticsearch 인덱스: http://localhost:9200/application-logs-*/_search"
