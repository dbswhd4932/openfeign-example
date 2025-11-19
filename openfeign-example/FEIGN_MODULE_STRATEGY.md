# OpenFeign 모듈 구성 전략

## 질문
상품 모듈이 추가되고, 다음과 같은 호출 관계가 있을 때:
- 주문 → 상품
- 회원 → 상품
- 상품 → 주문

**OpenFeign을 별도 모듈로 분리해야 할까? 아니면 각 모듈별로 관리해야 할까?**

---

## 결론: 각 모듈별로 OpenFeign을 관리하는 것을 추천

각 모듈이 자신이 필요한 Feign Client만 가지고 관리하는 것이 **독립성, 유지보수성, 배포 효율성** 측면에서 우수합니다.

---

## 상세 이유

### 1. 독립성과 응집도 (High Cohesion)

**각 모듈은 자신이 필요한 외부 호출만 관리해야 합니다.**

```
주문 모듈이 필요한 것:
- 상품 정보 조회 (ProductClient)
- 회원 정보 조회 (UserClient)

상품 모듈이 필요한 것:
- 주문 통계 조회 (OrderClient)

회원 모듈이 필요한 것:
- 상품 추천 조회 (ProductClient)
```

각 모듈이 **자신의 책임**에 집중하고, 필요한 클라이언트만 소유하는 것이 단일 책임 원칙(SRP)에 부합합니다.

---

### 2. 순환 참조 문제 방지

#### ❌ 공통 Feign 모듈 방식 (문제 발생)

```
주문 모듈 ─────┐
             ├──→ feign-client 모듈 (모든 Feign Client 포함)
상품 모듈 ─────┤      ├─ UserClient
             │      ├─ ProductClient
회원 모듈 ─────┘      └─ OrderClient
```

**문제점:**
- feign-client 모듈이 **비대해짐**
- 모든 모듈의 의존성이 한 곳에 집중
- 한 클라이언트 변경 시 **모든 모듈 영향**

#### ✅ 각 모듈별 관리 방식 (권장)

```
주문 모듈
├── UserClient       (회원 정보 조회)
└── ProductClient    (상품 정보 조회)

상품 모듈
└── OrderClient      (주문 통계 조회)

회원 모듈
└── ProductClient    (상품 추천 조회)
```

**장점:**
- 각 모듈이 **독립적**
- 필요한 의존성만 가짐
- 변경 영향 범위 최소화

---

### 3. 변경의 격리 (Isolation of Changes)

#### 시나리오: 상품 API 변경

**❌ 공통 Feign 모듈:**
```
1. 상품 API 스펙 변경
2. feign-client 모듈의 ProductClient 수정
3. feign-client 모듈 재배포
4. 주문/회원/상품 모듈 모두 재배포 필요 ⚠️
```

**✅ 각 모듈별 관리:**
```
1. 상품 API 스펙 변경
2. 주문 모듈의 ProductClient만 수정
3. 주문 모듈만 재배포 ✅
4. 회원 모듈은 필요할 때 업데이트
```

**변경 영향을 해당 모듈로만 제한할 수 있습니다.**

---

### 4. 배포 단위 최소화

#### 예제: ProductClient API 버전 업그레이드

**❌ 공통 Feign 모듈:**
```
상품 API v1 → v2 변경
↓
feign-client 모듈 업데이트
↓
주문 모듈 재배포 (v2 사용 준비 안됨) ⚠️
회원 모듈 재배포 (v2 사용 준비 안됨) ⚠️
상품 모듈 재배포
↓
전체 시스템 동시 배포 필요 (위험!)
```

**✅ 각 모듈별 관리:**
```
상품 API v1 → v2 변경
↓
주문 모듈: ProductClient v1 유지 (당분간 호환 모드)
회원 모듈: ProductClient v1 유지
상품 모듈: 먼저 v2로 업그레이드
↓
이후 점진적으로 각 모듈 업그레이드 ✅
```

**점진적 마이그레이션이 가능합니다.**

---

### 5. 버전 관리 유연성

각 모듈이 **독립적인 버전 관리**를 할 수 있습니다.

```java
// 주문 모듈 - 상품 API v2 사용
@FeignClient(name = "product-service", url = "${product.service.url}/v2")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}

// 회원 모듈 - 상품 API v1 사용 (아직 마이그레이션 안함)
@FeignClient(name = "product-service", url = "${product.service.url}/v1")
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}
```

**각 팀이 자신의 페이스대로 업그레이드 가능합니다.**

---

### 6. 테스트 격리

#### ✅ 각 모듈별 관리 시

```java
// 주문 모듈 테스트
@SpringBootTest
class OrderServiceTest {
    @MockBean
    private UserClient userClient;  // 주문이 필요한 것만 Mock
    
    @MockBean
    private ProductClient productClient;
    
    // 다른 모듈의 Feign Client는 신경쓸 필요 없음
}
```

**필요한 클라이언트만 Mock하면 됩니다.**

#### ❌ 공통 Feign 모듈 시

```java
// feign-client 모듈의 모든 클라이언트가 로드됨
// 불필요한 의존성까지 테스트 환경에 포함
```

---

## 권장 구조

### 디렉토리 구조

```
openfeign-example/
│
├── common/                         # 공통 DTO만
│   └── src/main/java/dto/
│       ├── User.java
│       ├── Product.java
│       └── Order.java
│
├── user-service/                   # 회원 서비스
│   └── src/main/java/
│       ├── UserServiceApplication.java
│       ├── controller/
│       │   └── UserController.java
│       ├── service/
│       │   └── UserService.java
│       └── client/                 # 회원이 필요한 클라이언트만
│           ├── ProductClient.java  # 상품 정보 조회
│           └── StubProductClient.java
│
├── product-service/                # 상품 서비스
│   └── src/main/java/
│       ├── ProductServiceApplication.java
│       ├── controller/
│       │   └── ProductController.java
│       ├── service/
│       │   └── ProductService.java
│       └── client/                 # 상품이 필요한 클라이언트만
│           ├── OrderClient.java    # 주문 통계 조회
│           └── StubOrderClient.java
│
└── order-service/                  # 주문 서비스
    └── src/main/java/
        ├── OrderServiceApplication.java
        ├── controller/
        │   └── OrderController.java
        ├── service/
        │   └── OrderService.java
        └── client/                 # 주문이 필요한 클라이언트만
            ├── UserClient.java     # 회원 정보 조회
            ├── StubUserClient.java
            ├── ProductClient.java  # 상품 정보 조회
            └── StubProductClient.java
```

---

## 공통화가 필요한 부분

### Feign 설정은 공통 모듈로 분리 가능

```
feign-common/                       # 설정만 공통화
├── build.gradle
└── src/main/java/config/
    ├── FeignConfig.java           # 공통 타임아웃, 로깅
    ├── AuthInterceptor.java       # 인증 헤더 추가
    └── FeignErrorDecoder.java     # 공통 에러 처리
```

#### 사용 예시

```java
// 각 모듈의 Feign Client
@FeignClient(
    name = "product-service",
    url = "${product.service.url}",
    configuration = FeignConfig.class  // 공통 설정 재사용
)
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);
}
```

---

## 예외: 공통 Feign 모듈이 나은 경우

### 1. 동일한 외부 서비스를 모든 모듈이 호출

```
모든 모듈 → 외부 결제 API (PG사)
모든 모듈 → 외부 알림 API (Slack, Email)
```

이런 경우는 **외부 API 클라이언트**로 분리하는 것이 좋습니다.

```
external-api-client/
├── PaymentClient.java
├── SlackClient.java
└── EmailClient.java
```

### 2. 공통 비즈니스 로직이 있는 경우

```java
// 모든 API 호출 시 공통 헤더 추가
public class CommonFeignClient {
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("X-Service-Name", serviceName);
        return headers;
    }
}
```

---

## 실무 팁

### 1. Stub 클라이언트는 필수

각 모듈별로 Stub 구현체를 만들어 **독립 테스트**를 가능하게 합니다.

```java
// 실제 구현
@FeignClient(name = "product-service")
@Profile("prod")
public interface ProductClient {
    ProductResponse getProduct(Long id);
}

// Stub 구현
@Component
@Profile("test")
public class StubProductClient implements ProductClient {
    public ProductResponse getProduct(Long id) {
        return new ProductResponse(id, "테스트 상품", 10000);
    }
}
```

### 2. API 계약 관리

각 모듈의 Feign Client는 **해당 서비스의 API 스펙**을 따라야 합니다.

```
product-service/
└── docs/
    └── api-spec.yaml  # OpenAPI 스펙

order-service/
└── client/
    └── ProductClient.java  # api-spec.yaml 기반으로 생성
```

### 3. 모니터링

각 모듈별로 Feign 호출을 **독립적으로 모니터링**할 수 있습니다.

```yaml
# order-service/application.yml
management:
  metrics:
    tags:
      service: order-service
      
feign:
  client:
    config:
      product-service:
        loggerLevel: FULL
```

---

## 비교 요약

| 항목 | 공통 Feign 모듈 | 각 모듈별 관리 (권장) |
|------|----------------|----------------------|
| **응집도** | ❌ 낮음 | ✅ 높음 |
| **독립 배포** | ❌ 어려움 | ✅ 쉬움 |
| **변경 영향** | ❌ 전체 모듈 | ✅ 해당 모듈만 |
| **버전 관리** | ❌ 통일 필요 | ✅ 독립 가능 |
| **테스트** | ❌ 복잡 | ✅ 간단 |
| **코드 중복** | ✅ 없음 | ⚠️ 일부 발생 (설정은 공통화 가능) |
| **유지보수** | ❌ 복잡 | ✅ 명확 |

---

## 결론

1. **Feign Client 인터페이스**: 각 모듈에서 관리 ✅
2. **Feign 설정 (Config)**: 공통 모듈로 분리 가능 ✅
3. **공통 DTO**: common 모듈에서 공유 ✅

이 구조로 **높은 응집도, 낮은 결합도**를 유지하면서도 코드 중복을 최소화할 수 있습니다.

### 핵심 원칙

> "각 모듈은 자신이 필요한 의존성만 가지고, 자신의 책임에만 집중한다."

이것이 **마이크로서비스 아키텍처의 본질**입니다.
