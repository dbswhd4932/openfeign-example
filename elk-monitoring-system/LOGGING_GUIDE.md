# Logback 설정 가이드

## 개요
이 프로젝트는 Logback과 Logstash Encoder를 사용하여 구조화된 JSON 로그를 생성하고 ELK 스택으로 전송합니다.

## 로깅 아키텍처

```
Spring Boot App
    ↓
Logback (logback-spring.xml)
    ↓
├─→ Console (개발용)
├─→ JSON File (로컬 저장)
├─→ Logstash TCP (실시간 전송)
└─→ Error File (에러만 별도 저장)
    ↓
Logstash (5044 포트)
    ↓
Elasticsearch
    ↓
Kibana (시각화)
```

## 주요 구성 요소

### 1. logback-spring.xml
Spring Boot의 Logback 설정 파일로 다음을 정의합니다:

#### Appender 종류

**CONSOLE**: 콘솔 출력 (개발용)
- 사람이 읽기 쉬운 형식
- 개발 환경에서 디버깅용

**JSON_FILE**: JSON 파일 저장
- 경로: `./logs/application.log`
- JSON 형식으로 저장
- 롤링 정책: 10MB, 30일 보관

**LOGSTASH**: TCP를 통한 실시간 전송
- 주소: localhost:5044
- 비동기 처리로 성능 최적화
- 재연결 자동 처리

**ERROR_FILE**: 에러 로그만 별도 저장
- 경로: `./logs/error.log`
- ERROR 레벨만 필터링
- 60일 보관 (일반 로그보다 길게)

### 2. MDC (Mapped Diagnostic Context)
요청마다 컨텍스트 정보를 추적합니다:

- **requestId**: 각 HTTP 요청의 고유 ID (UUID)
- **userId**: 사용자 식별자
- **clientIp**: 클라이언트 IP 주소

### 3. LoggingInterceptor
모든 HTTP 요청/응답을 자동으로 로깅:
- 요청 시작 시 MDC 설정
- 요청 메서드, URI, IP 로깅
- 응답 상태 코드 로깅
- 요청 종료 시 MDC 클리어

### 4. GlobalExceptionHandler
모든 예외를 캐치하여 로깅:
- BusinessException: WARN 레벨
- IllegalArgumentException: WARN 레벨
- 일반 Exception: ERROR 레벨 (스택 트레이스 포함)

### 5. LoggingUtils
구조화된 로깅 헬퍼:
- 비즈니스 이벤트 로깅
- 성능 측정 로깅
- 커스텀 필드 추가

## JSON 로그 구조

생성되는 JSON 로그 예시:

```json
{
  "@timestamp": "2025-11-07T09:45:23.123+09:00",
  "level": "INFO",
  "logger_name": "com.example.elkmonitoring.controller.UserController",
  "message": "User created successfully",
  "thread_name": "http-nio-8080-exec-1",
  "app_name": "elk-monitoring-system",
  "environment": "development",
  "requestId": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "user123",
  "clientIp": "127.0.0.1",
  "stack_trace": "...",
  "custom_field": "custom_value"
}
```

## 로그 레벨 정책

### DEBUG
- 개발 환경에서 상세 정보
- 패키지: com.example.elkmonitoring

### INFO
- 일반적인 정보성 메시지
- HTTP 요청/응답
- 비즈니스 이벤트

### WARN
- 경고성 메시지
- BusinessException
- 예상 가능한 오류

### ERROR
- 심각한 오류
- 예상하지 못한 예외
- 시스템 장애

## 사용 예제

### 1. 기본 로깅

```java
@Slf4j
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        // ...
    }
}
```

### 2. 구조화된 로깅

```java
@Slf4j
@Service
public class UserService {

    public void createUser(User user) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("user_id", user.getId());
        fields.put("user_email", user.getEmail());

        LoggingUtils.logBusinessEvent(log, "user_created", fields);
    }
}
```

### 3. 성능 측정

```java
@Slf4j
@Service
public class ProductService {

    public List<Product> findProducts() {
        long startTime = System.currentTimeMillis();

        List<Product> products = productRepository.findAll();

        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> metadata = Map.of(
            "product_count", products.size()
        );

        LoggingUtils.logPerformance(log, "find_all_products", duration, metadata);

        return products;
    }
}
```

### 4. 예외 로깅

```java
@Service
public class OrderService {

    public void processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                "Order not found: " + orderId,
                "ORDER_NOT_FOUND",
                HttpStatus.NOT_FOUND
            ));
    }
}
```

## 로그 파일 위치

```
./logs/
├── application.log          # 전체 로그 (JSON)
├── application.2025-11-07.0.log  # 롤링된 파일
├── error.log               # 에러 로그만
└── error.2025-11-07.0.log  # 롤링된 에러 파일
```

## 성능 최적화

### 비동기 Appender
- 로그 쓰기가 애플리케이션 성능에 영향을 주지 않도록 비동기 처리
- 큐 크기: 512
- discardingThreshold: 0 (로그 손실 방지)

### 재연결 처리
- Logstash 연결 실패 시 10초마다 재시도
- 연결 실패해도 애플리케이션 정상 동작

### 롤링 정책
- 파일 크기: 10MB
- 보관 기간: 30일 (에러는 60일)
- 전체 크기 제한: 1GB

## 트러블슈팅

### Logstash 연결 실패
```
ERROR c.l.l.a.LogstashTcpSocketAppender - Connection refused
```
**해결**: Logstash가 실행 중인지 확인
```bash
docker-compose logs logstash
curl http://localhost:9600/_node/stats
```

### 로그 파일 생성 안됨
**해결**: logs 디렉토리 권한 확인
```bash
mkdir -p logs
chmod 755 logs
```

### MDC 값이 로그에 안나옴
**해결**: Interceptor가 등록되었는지 확인
- WebConfig에서 LoggingInterceptor 등록 확인
- 제외 패턴에 해당하지 않는지 확인

## 다음 단계
- 4단계: REST API 및 예제 코드 작성
- 실제 로그 생성 및 ELK 스택 연동 테스트
