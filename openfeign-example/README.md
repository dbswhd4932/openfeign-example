# OpenFeign 학습 예제 프로젝트

Spring Cloud OpenFeign을 사용한 마이크로서비스 간 통신 학습 프로젝트입니다.

## 프로젝트 구조

이 프로젝트는 **멀티 모듈 구조**로 구성된 프로덕션 레디 아키텍처입니다:

### 모듈 구성

1. **common**
   - 공통 DTO 및 유틸리티 클래스
   - User DTO 정의

2. **user-service** (포트 8080)
   - 사용자 정보를 제공하는 독립 서비스
   - REST API를 통해 사용자 CRUD 작업 제공
   - common 모듈 의존

3. **order-service** (포트 8081)
   - 주문 정보를 관리하는 독립 서비스
   - common 모듈 의존
   - User Service와 통신하기 위한 두 가지 클라이언트 구현
     - **RestUserClient**: OpenFeign을 사용한 실제 HTTP 호출 (프로덕션)
     - **StubUserClient**: 메모리 기반 테스트용 구현 (개발/테스트)
   - 주문 조회 시 자동으로 사용자 정보를 가져옴

## 기술 스택

- Spring Boot 3.2.0
- Spring Cloud OpenFeign
- Lombok
- Java 17

## OpenFeign 주요 기능

### 1. @FeignClient 어노테이션
```java
@FeignClient(
    name = "user-service",
    url = "${user.service.url}",
    configuration = FeignConfig.class
)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
```

### 2. 설정 옵션
- **타임아웃 설정**: 연결 및 읽기 타임아웃
- **재시도 정책**: 실패 시 재시도 로직
- **로깅 레벨**: 요청/응답 로깅 상세도

### 3. 로깅 레벨
- `NONE`: 로깅 안함
- `BASIC`: 요청 메서드, URL, 응답 상태, 실행 시간
- `HEADERS`: BASIC + 헤더 정보
- `FULL`: 모든 요청/응답 데이터

## 실행 방법

### 방법 1: User Service 단독 실행

```bash
./gradlew :user-service:bootRun
```

### 방법 2: Order Service + REST 클라이언트 (실제 HTTP 호출)

실제 프로덕션 환경과 동일하게 HTTP를 통해 User Service를 호출합니다.

```bash
# 터미널 1: User Service 실행
./gradlew :user-service:bootRun

# 터미널 2: Order Service (REST 모드)
./gradlew :order-service:bootRun --args='--spring.profiles.active=rest'
```

**REST 모드 특징:**
- `RestUserClient` 사용 (OpenFeign 기반 HTTP 호출)
- User Service가 반드시 실행 중이어야 함
- 실제 네트워크 호출 발생
- Feign의 타임아웃, 재시도, 로깅 등 모든 기능 확인 가능

### 방법 3: Order Service + Stub 클라이언트 (독립 실행) ⭐ 개발/테스트 추천

User Service 없이도 Order Service를 독립적으로 실행하고 테스트할 수 있습니다.

```bash
./gradlew :order-service:bootRun --args='--spring.profiles.active=stub'
```

**Stub 모드 특징:**
- `StubUserClient` 사용 (메모리 기반)
- User Service 실행 불필요
- 네트워크 호출 없어서 빠른 테스트
- 외부 의존성 제거로 안정적인 개발 환경
- 🔧 접두사가 붙은 로그로 Stub 호출 확인

**실행 예시:**
```bash
# Stub 모드로 실행
./gradlew :order-service:bootRun --args='--spring.profiles.active=stub'

# 로그에서 확인:
# 🔧 [STUB MODE] StubUserClient initialized with 3 users

# API 호출
curl http://localhost:8081/api/orders/1

# 로그에 표시:
# 🔧 [STUB] getUserById called with id: 1
```

## 클라이언트 선택 가이드

| 상황 | 실행 명령 | 사용 클라이언트 | User Service 필요 |
|------|----------|----------------|------------------|
| **프로덕션** | `./gradlew :order-service:bootRun --args='--spring.profiles.active=rest'` | RestUserClient | ✅ 필수 |
| **통합 테스트** | `./gradlew :order-service:bootRun --args='--spring.profiles.active=rest'` | RestUserClient | ✅ 필수 |
| **개발/단위 테스트** | `./gradlew :order-service:bootRun --args='--spring.profiles.active=stub'` | StubUserClient | ❌ 불필요 |
| **로컬 개발** | `./gradlew :order-service:bootRun --args='--spring.profiles.active=stub'` | StubUserClient | ❌ 불필요 |

## API 테스트

### User Service API (포트 8080)

#### 1. 모든 사용자 조회
```bash
curl http://localhost:8080/api/users
```

#### 2. 특정 사용자 조회
```bash
curl http://localhost:8080/api/users/1
```

#### 3. 사용자 생성
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4,
    "name": "최지훈",
    "email": "choi@example.com",
    "phone": "010-4567-8901"
  }'
```

### Order Service API (포트 8081)

#### 1. 모든 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders
```
이 요청은 내부적으로 User Service를 호출하여 각 주문의 사용자 정보를 가져옵니다.

#### 2. 특정 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders/1
```

#### 3. 특정 사용자의 주문 조회 (Feign 사용)
```bash
curl http://localhost:8081/api/orders/user/1
```

#### 4. 새 주문 생성 (Feign으로 사용자 검증)
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "id": 4,
    "userId": 2,
    "productName": "모니터",
    "quantity": 1,
    "price": 350000.0
  }'
```

## 학습 포인트

### 1. Feign Client 인터페이스 정의
`UserClient.java` 파일을 확인하여 Feign Client를 어떻게 정의하는지 학습하세요.

### 2. Feign 설정
`FeignConfig.java` 파일에서 타임아웃, 재시도, 로깅 등을 설정하는 방법을 확인하세요.

### 3. Feign 사용
`OrderService.java` 파일에서 Feign Client를 주입받아 사용하는 방법을 확인하세요.

### 4. Stub 구현으로 인터페이스 기반 개발 학습 ⭐ 신규
`StubUserClient.java` 파일에서 인터페이스 기반 개발의 장점을 학습하세요:
- 같은 인터페이스(`UserClient`)를 구현하는 두 가지 방법
  - **실제 구현**: Feign이 자동 생성 (HTTP 호출)
  - **Stub 구현**: 메모리 기반 데이터 (HTTP 호출 없음)
- `@Profile` 어노테이션으로 런타임에 구현체 교체
- 의존성 역전 원칙(DIP) 적용 사례
- 테스트 용이성 향상

**비교:**
```java
// OrderService는 인터페이스에만 의존
private final UserClient userClient;

// 실행 시점에 결정:
// - order-service 프로필: Feign 구현체 (실제 HTTP)
// - order-service,stub 프로필: StubUserClient (메모리)
```

### 5. 로깅 확인
Order Service를 실행하고 API를 호출하면 콘솔에서 Feign의 상세한 로그를 확인할 수 있습니다:
- 요청 URL
- 요청 헤더
- 요청 바디
- 응답 상태
- 응답 헤더
- 응답 바디
- 실행 시간

**Stub 모드에서는:**
- 🔧 접두사가 붙은 로그로 Stub 호출 확인 가능
- 실제 HTTP 호출 로그는 없음

## 에러 처리 테스트

### 1. 존재하지 않는 사용자로 주문 생성 시도
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "id": 5,
    "userId": 999,
    "productName": "테스트 상품",
    "quantity": 1,
    "price": 10000.0
  }'
```
Feign이 404 에러를 어떻게 처리하는지 확인할 수 있습니다.

### 2. User Service 중단 후 Order Service 호출
User Service를 중단한 후 Order Service API를 호출하면 타임아웃과 재시도 로직을 확인할 수 있습니다.

## 고급 학습 주제

### 1. Feign Fallback
서비스가 실패했을 때 대체 로직을 실행하도록 설정할 수 있습니다.

### 2. Feign Interceptor
모든 요청에 공통 헤더(예: 인증 토큰)를 추가할 수 있습니다.

### 3. Circuit Breaker
Resilience4j와 통합하여 서킷 브레이커 패턴을 적용할 수 있습니다.

### 4. Service Discovery
Eureka 또는 Consul과 통합하여 동적 서비스 디스커버리를 구현할 수 있습니다.

## 참고 자료

- [Spring Cloud OpenFeign 공식 문서](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [Feign GitHub](https://github.com/OpenFeign/feign)

## 디렉토리 구조

```
openfeign-example/                              # 루트 프로젝트
├── build.gradle                                 # 루트 빌드 설정
├── settings.gradle                              # 멀티 모듈 설정
├── README.md                                    # 프로젝트 문서
├── api-tests.http                               # HTTP 테스트 파일
│
├── common/                                      # 공통 모듈
│   ├── build.gradle
│   └── src/main/java/com/example/openfeign/common/
│       └── User.java                            # 공통 User DTO
│
├── user-service/                                # User Service 모듈
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/example/openfeign/
│       │   │   ├── UserServiceApplication.java  # User Service 메인
│       │   │   └── user/
│       │   │       └── UserController.java      # User API
│       │   └── resources/
│       │       └── application.yml              # User Service 설정
│
└── order-service/                               # Order Service 모듈
    ├── build.gradle
    └── src/
        ├── main/
        │   ├── java/com/example/openfeign/
        │   │   ├── OrderServiceApplication.java # Order Service 메인
        │   │   └── order/
        │   │       ├── Order.java               # Order DTO
        │   │       ├── UserClient.java          # Feign Client 인터페이스
        │   │       ├── RestUserClient.java      # Feign 기반 HTTP 구현체
        │   │       ├── StubUserClient.java      # 메모리 기반 Stub 구현체
        │   │       ├── FeignConfig.java         # Feign 설정
        │   │       ├── OrderService.java        # Order 비즈니스 로직
        │   │       └── OrderController.java     # Order API
        │   └── resources/
        │       └── application.yml              # Order Service 설정 (REST/Stub 프로필)
```
