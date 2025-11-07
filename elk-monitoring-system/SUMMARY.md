# ELK Monitoring System - 학습 요약

## 프로젝트 완성 현황

### ✅ 완료된 단계

1. **Spring Boot 프로젝트 기본 구조 생성**
2. **Docker Compose로 ELK 스택 환경 구성**
3. **Spring Boot 애플리케이션에 Logback 설정**
4. **간단한 REST API 및 예제 코드 작성**
5. **Logstash 파이프라인 설정 확인 및 테스트**
6. **Kibana 대시보드 및 알림 설정**
7. **전체 통합 테스트 및 문서화**

---

## 시스템 구성 요소

### 1. Infrastructure (Docker)
- **MySQL 8.0**: 애플리케이션 데이터베이스
- **Elasticsearch 8.11.0**: 로그 저장 및 검색 엔진
- **Logstash 8.11.0**: 로그 수집 및 변환 파이프라인
- **Kibana 8.11.0**: 로그 시각화 대시보드

### 2. Application Stack
- **Spring Boot 3.2.0**: 백엔드 프레임워크
- **Spring Data JPA**: 데이터 접근 계층
- **Logback + Logstash Encoder**: 구조화된 로깅

### 3. 개발 도구
- **Java 17**: 프로그래밍 언어
- **Gradle 8.5**: 빌드 도구
- **Docker Compose**: 컨테이너 오케스트레이션

---

## 구현된 주요 기능

### 📊 로깅 기능

#### 1. 구조화된 JSON 로깅
- Logstash Encoder를 사용한 JSON 로그 생성
- 일관된 로그 구조로 검색 및 분석 용이

#### 2. 다중 Appender
- **Console**: 개발 환경 디버깅
- **JSON File**: 로컬 파일 저장 (롤링 정책 적용)
- **Logstash TCP**: 실시간 로그 전송 (5044 포트)
- **Error File**: 에러 로그 별도 저장

#### 3. MDC (Mapped Diagnostic Context)
- **requestId**: 각 HTTP 요청의 고유 ID
- **userId**: 사용자 식별자
- **clientIp**: 클라이언트 IP 주소
- 요청 전체 흐름 추적 가능

#### 4. 비즈니스 이벤트 로깅
- `user_created`: 사용자 생성 이벤트
- `user_updated`: 사용자 수정 이벤트
- `user_deleted`: 사용자 삭제 이벤트
- `user_status_changed`: 사용자 상태 변경 이벤트

#### 5. 성능 메트릭
- 작업 실행 시간 측정
- `duration_ms` 필드로 성능 분석

---

### 🔍 검색 및 필터링

#### Logstash 파이프라인
```
Input (TCP + File)
  ↓
Filter (JSON 파싱, 태깅, 필드 추출)
  ↓
Output (Elasticsearch + 콘솔)
```

#### 필터링 규칙
- JSON 메시지 파싱
- 타임스탬프 정규화
- 로그 레벨별 태그 추가
- ERROR 레벨에 `alert_required: true` 태그
- 컴포넌트 자동 추출 (logger_name 기반)

---

### 📈 시각화 및 모니터링

#### Kibana 대시보드 구성요소
1. **Log Level Distribution**: 로그 레벨별 분포 (파이 차트)
2. **Logs Over Time**: 시간별 로그 추이 (라인 차트)
3. **Error Logs Over Time**: 에러 발생 추이 (영역 차트)
4. **Business Events**: 비즈니스 이벤트 분포 (막대 차트)
5. **Top Errors**: 빈번한 에러 메시지 (테이블)

#### 알림 규칙
1. **High Error Rate Alert**
   - 조건: 5분간 에러 로그 5개 이상
   - 액션: Server log (운영 시 Slack, Email 등)

2. **Business Event Failure Alert**
   - 조건: 비즈니스 이벤트 실패 발생
   - 액션: 즉시 알림

---

## 테스트 결과

### 통합 테스트 (test-integration.sh)

```
✓ MySQL: Running
✓ Elasticsearch: Running (Status: yellow)
✓ Logstash: Running
✓ Kibana: Running
✓ Health Check API
✓ User Creation API
✓ Get User API
✓ Error Simulation
✓ Elasticsearch Indices
✓ Log Count: 211+
✓ Business Events: 11+
✓ Error Logs: 11+
✓ MDC Fields (requestId, clientIp)

Total: 15/15 테스트 통과
```

### 로그 통계
- **총 로그**: 211개+
- **INFO**: 149개
- **DEBUG**: 43개
- **ERROR**: 11개
- **WARN**: 8개
- **비즈니스 이벤트**: 11개

---

## 학습 성과

### 1. ELK Stack 이해도
✅ Elasticsearch: 로그 저장 및 검색 메커니즘 이해
✅ Logstash: 파이프라인 구성 및 데이터 변환 학습
✅ Kibana: 시각화 및 대시보드 구성 능력 습득

### 2. 로깅 Best Practices
✅ 구조화된 로깅의 중요성 이해
✅ JSON 형식의 장점 (검색, 분석, 통합)
✅ 로그 레벨의 적절한 사용
✅ MDC를 통한 컨텍스트 전파

### 3. Docker & 컨테이너 기술
✅ Docker Compose를 통한 멀티 컨테이너 관리
✅ 서비스 간 네트워킹 구성
✅ 볼륨을 통한 데이터 영속성
✅ 헬스체크를 통한 의존성 관리

### 4. 모니터링 및 장애 대응
✅ 실시간 로그 모니터링 체계 구축
✅ 알림 규칙 설정
✅ 에러 추적 및 분석 방법
✅ 비즈니스 메트릭 수집

### 5. Spring Boot 고급 기능
✅ Interceptor를 통한 요청 추적
✅ 전역 예외 처리
✅ AOP 패턴 (로깅 관점)
✅ 구조화된 애플리케이션 아키텍처

---

## 프로젝트 파일 구조

```
elk-monitoring-system/
├── 📁 src/main/
│   ├── java/com/example/elkmonitoring/
│   │   ├── config/          # 설정 (Interceptor, Utils)
│   │   ├── controller/      # REST API
│   │   ├── service/         # 비즈니스 로직
│   │   ├── repository/      # 데이터 접근
│   │   ├── domain/          # 엔티티
│   │   ├── dto/             # DTO
│   │   └── exception/       # 예외 처리
│   └── resources/
│       ├── application.yml
│       └── logback-spring.xml
├── 📁 logstash/
│   ├── config/logstash.yml
│   └── pipeline/logstash.conf
├── 📁 logs/                 # 로그 파일
├── 📄 docker-compose.yml     # Docker 구성
├── 📄 build.gradle          # Gradle 빌드
├── 📜 start.sh              # 시작 스크립트
├── 📜 generate-logs.sh      # 로그 생성
├── 📜 test-integration.sh   # 통합 테스트
└── 📚 문서/
    ├── README.md
    ├── DOCKER_GUIDE.md
    ├── LOGGING_GUIDE.md
    ├── API_EXAMPLES.md
    ├── KIBANA_GUIDE.md
    └── SUMMARY.md (이 파일)
```

---

## 실행 방법 요약

### 1. 전체 시스템 시작
```bash
# Docker 서비스 시작
docker-compose up -d

# Spring Boot 앱 시작
./gradlew bootRun
```

### 2. 로그 생성
```bash
# 테스트 로그 생성
./generate-logs.sh
```

### 3. 통합 테스트
```bash
# 전체 시스템 테스트
./test-integration.sh
```

### 4. 모니터링
- **Kibana**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200
- **Spring Boot**: http://localhost:8080

---

## 주요 학습 포인트

### 로깅 아키텍처
```
Application → Logback → Logstash → Elasticsearch → Kibana
              ↓
         (JSON File)
```

### 데이터 흐름
1. Spring Boot에서 로그 발생
2. Logback이 JSON으로 인코딩
3. Logstash TCP로 실시간 전송
4. Logstash 파이프라인에서 필터링/변환
5. Elasticsearch에 인덱싱
6. Kibana에서 시각화

### 주요 설정 파일
- `logback-spring.xml`: 로그 출력 설정
- `logstash/pipeline/logstash.conf`: 로그 변환 규칙
- `docker-compose.yml`: 인프라 구성

---

## 확장 가능성

### 운영 환경 적용 시
1. **보안 강화**
   - Elasticsearch 사용자 인증
   - SSL/TLS 암호화
   - API Key 기반 접근 제어

2. **고가용성**
   - Elasticsearch 클러스터 (3+ 노드)
   - Logstash 로드 밸런싱
   - Kibana 다중 인스턴스

3. **통합**
   - Slack 알림 연동
   - Email 알림
   - PagerDuty 통합
   - APM (Application Performance Monitoring)

4. **성능 최적화**
   - 인덱스 샤딩 전략
   - ILM (Index Lifecycle Management)
   - Hot-Warm-Cold 아키텍처

---

## 마무리

이 프로젝트를 통해 다음을 학습했습니다:

✅ **ELK Stack의 실전 활용**
✅ **구조화된 로깅 시스템 구축**
✅ **Docker 기반 인프라 구성**
✅ **실시간 모니터링 및 알림 체계**
✅ **Spring Boot 고급 기능 활용**

### 다음 학습 단계 제안
1. APM (Elastic APM) 통합
2. 분산 추적 (Distributed Tracing)
3. 메트릭 수집 (Metricbeat)
4. 로그 보안 및 암호화
5. Kubernetes 환경에서의 ELK 구성

---

## 참고 자료

- [Elasticsearch 공식 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Logstash 공식 문서](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Kibana 공식 문서](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Logback 공식 문서](https://logback.qos.ch/manual/index.html)
- [Spring Boot 로깅 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)

---

**프로젝트 완료일**: 2025-11-07
**상태**: ✅ 완료
**테스트 결과**: 15/15 통과
