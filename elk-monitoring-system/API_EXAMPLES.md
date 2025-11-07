# API 사용 예제

## 개요
User 관리 REST API 예제 및 테스트 방법

## 기본 정보
- Base URL: `http://localhost:8080`
- Content-Type: `application/json`

## API 엔드포인트

### 1. 헬스 체크

**요청**
```bash
curl -X GET http://localhost:8080/health
```

**응답**
```json
{
  "status": "UP",
  "timestamp": "2025-11-07T10:00:00",
  "application": "elk-monitoring-system"
}
```

---

### 2. 모든 사용자 조회

**요청**
```bash
curl -X GET http://localhost:8080/api/users
```

**응답**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678",
    "status": "ACTIVE",
    "createdAt": "2025-11-07T10:00:00",
    "updatedAt": "2025-11-07T10:00:00"
  }
]
```

---

### 3. 특정 사용자 조회

**요청**
```bash
curl -X GET http://localhost:8080/api/users/1
```

**응답**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "status": "ACTIVE",
  "createdAt": "2025-11-07T10:00:00",
  "updatedAt": "2025-11-07T10:00:00"
}
```

**에러 응답 (사용자 없음)**
```json
{
  "timestamp": "2025-11-07T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "errorCode": "USER_NOT_FOUND",
  "path": "/api/users/999"
}
```

---

### 4. 사용자 생성

**요청**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678"
  }'
```

**응답** (201 Created)
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "status": "ACTIVE",
  "createdAt": "2025-11-07T10:00:00",
  "updatedAt": "2025-11-07T10:00:00"
}
```

**에러 응답 (이메일 중복)**
```json
{
  "timestamp": "2025-11-07T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: hong@example.com",
  "errorCode": "DUPLICATE_EMAIL",
  "path": "/api/users"
}
```

---

### 5. 사용자 수정

**요청**
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동 수정",
    "email": "hong.updated@example.com",
    "phone": "010-9876-5432"
  }'
```

**응답**
```json
{
  "id": 1,
  "name": "홍길동 수정",
  "email": "hong.updated@example.com",
  "phone": "010-9876-5432",
  "status": "ACTIVE",
  "createdAt": "2025-11-07T10:00:00",
  "updatedAt": "2025-11-07T10:05:00"
}
```

---

### 6. 사용자 삭제

**요청**
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

**응답** (204 No Content)
```
(응답 본문 없음)
```

---

### 7. 사용자 상태 변경

**요청**
```bash
curl -X PATCH "http://localhost:8080/api/users/1/status?status=SUSPENDED"
```

**가능한 상태값**
- `ACTIVE`: 활성
- `INACTIVE`: 비활성
- `SUSPENDED`: 정지

**응답**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "status": "SUSPENDED",
  "createdAt": "2025-11-07T10:00:00",
  "updatedAt": "2025-11-07T10:10:00"
}
```

---

### 8. 에러 시뮬레이션 (테스트용)

**요청**
```bash
curl -X GET http://localhost:8080/api/users/test/error
```

**목적**
- ELK 스택에서 에러 로그 수집 테스트
- 에러 알림 테스트

**응답** (500 Internal Server Error)
```json
{
  "timestamp": "2025-11-07T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "errorCode": "INTERNAL_SERVER_ERROR",
  "path": "/api/users/test/error"
}
```

---

## 테스트 시나리오

### 시나리오 1: 정상 플로우
```bash
# 1. 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","email":"kim@example.com","phone":"010-1111-2222"}'

# 2. 모든 사용자 조회
curl -X GET http://localhost:8080/api/users

# 3. 특정 사용자 조회 (ID는 응답에서 확인)
curl -X GET http://localhost:8080/api/users/1

# 4. 사용자 수정
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수 수정","email":"kim.updated@example.com","phone":"010-3333-4444"}'

# 5. 상태 변경
curl -X PATCH "http://localhost:8080/api/users/1/status?status=INACTIVE"

# 6. 사용자 삭제
curl -X DELETE http://localhost:8080/api/users/1
```

### 시나리오 2: 에러 케이스
```bash
# 1. 존재하지 않는 사용자 조회
curl -X GET http://localhost:8080/api/users/999

# 2. 중복 이메일로 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","phone":"010-1234-5678"}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동2","email":"hong@example.com","phone":"010-5678-1234"}'

# 3. 잘못된 이메일 형식
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트","email":"invalid-email","phone":"010-1234-5678"}'

# 4. 의도적 에러 발생
curl -X GET http://localhost:8080/api/users/test/error
```

---

## 로그 확인

각 API 호출 시 다음과 같은 로그가 생성됩니다:

### Elasticsearch에서 확인할 로그 필드
- `@timestamp`: 로그 발생 시간
- `level`: 로그 레벨 (INFO, WARN, ERROR)
- `logger_name`: 로거 이름
- `message`: 로그 메시지
- `requestId`: HTTP 요청 ID (MDC)
- `userId`: 사용자 ID (MDC)
- `clientIp`: 클라이언트 IP (MDC)
- `event_name`: 비즈니스 이벤트 이름
- `event_type`: 이벤트 타입 (business_event, performance 등)

### 비즈니스 이벤트
- `user_created`: 사용자 생성
- `user_updated`: 사용자 수정
- `user_deleted`: 사용자 삭제
- `user_status_changed`: 사용자 상태 변경

---

## 다음 단계
API 호출 후 Kibana에서 로그를 확인하고 대시보드를 구성합니다.
