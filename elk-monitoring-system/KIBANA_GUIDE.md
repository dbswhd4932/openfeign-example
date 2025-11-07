# Kibana 대시보드 및 알림 설정 가이드

## 개요
Kibana를 통해 로그를 시각화하고, 대시보드를 구성하며, 알림을 설정하는 방법을 설명합니다.

## Kibana 접속
- URL: http://localhost:5601
- 인증: 없음 (개발 환경)

## 1단계: Data View (인덱스 패턴) 생성

### 1-1. Kibana 메인 페이지 접속
1. 브라우저에서 http://localhost:5601 접속
2. 좌측 메뉴에서 "Management" → "Stack Management" 클릭

### 1-2. Data View 생성
1. "Kibana" 섹션에서 "Data Views" 클릭
2. "Create data view" 버튼 클릭
3. 설정 입력:
   - **Name**: `Application Logs`
   - **Index pattern**: `application-logs-*`
   - **Timestamp field**: `@timestamp`
4. "Save data view to Kibana" 클릭

**검증**: Data view가 생성되면 Discover 메뉴에서 로그를 볼 수 있습니다.

---

## 2단계: Discover에서 로그 탐색

### 2-1. Discover 메뉴 접속
1. 좌측 메뉴에서 "Analytics" → "Discover" 클릭
2. 상단에서 "Application Logs" data view 선택

### 2-2. 유용한 필드 추가
좌측 필드 목록에서 다음 필드를 클릭하여 테이블에 추가:
- `level` - 로그 레벨
- `logger_name` - 로거 이름
- `message` - 로그 메시지
- `requestId` - 요청 ID
- `userId` - 사용자 ID
- `event_name` - 이벤트 이름
- `event_type` - 이벤트 타입

### 2-3. 필터 사용 예시

**에러 로그만 보기**:
```
level: "ERROR"
```

**특정 이벤트 검색**:
```
event_name: "user_created"
```

**비즈니스 이벤트만 보기**:
```
event_type: "business_event"
```

**특정 컴포넌트 로그**:
```
component: "service"
```

---

## 3단계: 시각화(Visualization) 생성

### 3-1. 로그 레벨별 분포 (파이 차트)

1. "Analytics" → "Visualize Library" → "Create visualization"
2. "Pie" 차트 선택
3. Data view: "Application Logs" 선택
4. 설정:
   - **Slice by**: `level.keyword`
   - **Size by**: Count
5. "Save" → 이름: "Log Level Distribution"

### 3-2. 시간별 로그 추이 (시계열 그래프)

1. "Create visualization" → "Line" 선택
2. 설정:
   - **Vertical axis**: Count
   - **Horizontal axis**: `@timestamp`
   - **Break down by**: `level.keyword`
3. "Save" → 이름: "Logs Over Time"

### 3-3. 에러 로그 발생 추이

1. "Create visualization" → "Area" 선택
2. 필터 추가: `level: "ERROR"`
3. 설정:
   - **Vertical axis**: Count
   - **Horizontal axis**: `@timestamp`
4. "Save" → 이름: "Error Logs Over Time"

### 3-4. 비즈니스 이벤트 분포

1. "Create visualization" → "Bar (vertical)" 선택
2. 필터 추가: `event_type: "business_event"`
3. 설정:
   - **Vertical axis**: Count
   - **Horizontal axis**: `event_name.keyword`
4. "Save" → 이름: "Business Events"

### 3-5. Top 10 에러 메시지

1. "Create visualization" → "Table" 선택
2. 필터 추가: `level: "ERROR"`
3. 설정:
   - **Rows**: `message.keyword` (Top 10)
   - **Metrics**: Count
4. "Save" → 이름: "Top Errors"

---

## 4단계: 대시보드 생성

### 4-1. 대시보드 만들기

1. "Analytics" → "Dashboard" → "Create dashboard"
2. "Add from library" 클릭
3. 이전에 만든 시각화 추가:
   - Log Level Distribution
   - Logs Over Time
   - Error Logs Over Time
   - Business Events
   - Top Errors

### 4-2. 레이아웃 조정

- 시각화를 드래그하여 배치
- 크기 조정
- 추천 레이아웃:
  ```
  +---------------------------+---------------------------+
  |   Log Level Distribution  |     Business Events       |
  +---------------------------+---------------------------+
  |           Logs Over Time (전체 너비)                  |
  +-------------------------------------------------------+
  |        Error Logs Over Time (전체 너비)               |
  +-------------------------------------------------------+
  |            Top Errors (전체 너비)                     |
  +-------------------------------------------------------+
  ```

### 4-3. 저장

- 우측 상단 "Save" 클릭
- 이름: "Application Monitoring Dashboard"
- "Save" 클릭

---

## 5단계: 알림(Alerting) 설정

Kibana 8.x에서는 Rules and Connectors를 사용하여 알림을 설정합니다.

### 5-1. 에러 로그 알림 규칙 생성

1. "Management" → "Stack Management" → "Rules and Connectors"
2. "Rules" 탭 → "Create rule" 클릭
3. 규칙 설정:

**기본 정보**:
- **Name**: `High Error Rate Alert`
- **Rule type**: `Elasticsearch query`

**Query 설정**:
```json
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "level": "ERROR"
          }
        },
        {
          "range": {
            "@timestamp": {
              "gte": "now-5m"
            }
          }
        }
      ]
    }
  }
}
```

**Index**: `application-logs-*`

**Time field**: `@timestamp`

**Threshold**:
- When: `count()`
- IS ABOVE: `5`
- FOR THE LAST: `5 minutes`

**Actions**:
- Server log (기본 - 콘솔에 로그 출력)
- 실제 운영에서는 Slack, Email, PagerDuty 등 설정 가능

4. "Save" 클릭

### 5-2. 비즈니스 이벤트 실패 알림

1. "Create rule" 클릭
2. 규칙 설정:
   - **Name**: `Business Event Failure Alert`
   - **Rule type**: `Elasticsearch query`
   - **Query**:
     ```json
     {
       "query": {
         "bool": {
           "must": [
             {
               "match": {
                 "event_type": "business_event"
               }
             },
             {
               "match": {
                 "level": "ERROR"
               }
             }
           ]
         }
       }
     }
     ```
   - **Threshold**: IS ABOVE 0 FOR THE LAST 1 minute

---

## 6단계: 저장된 검색(Saved Searches) 생성

### 6-1. 에러 로그 저장된 검색

1. "Discover"로 이동
2. 검색: `level: "ERROR"`
3. 필드 추가: `message`, `logger_name`, `stack_trace`, `requestId`
4. 우측 상단 "Save" → 이름: "Error Logs"

### 6-2. 비즈니스 이벤트 저장된 검색

1. 검색: `event_type: "business_event"`
2. 필드 추가: `event_name`, `user_id`, `user_email`, `message`
3. "Save" → 이름: "Business Events"

### 6-3. 사용자 생성 이벤트

1. 검색: `event_name: "user_created"`
2. 필드 추가: `user_name`, `user_email`, `user_status`
3. "Save" → 이름: "User Created Events"

---

## 7단계: 고급 기능

### 7-1. Metric Visualization (메트릭)

**평균 응답 시간**:
1. "Create visualization" → "Metric"
2. 필터: `duration_ms: *`
3. Metric: Average of `duration_ms`
4. "Save" → 이름: "Average Response Time"

### 7-2. Heat Map (시간대별 로그 분포)

1. "Create visualization" → "Heat map"
2. 설정:
   - **Vertical axis**: Hour of `@timestamp`
   - **Horizontal axis**: Day of week of `@timestamp`
   - **Cell value**: Count

### 7-3. Tag Cloud (자주 발생하는 에러)

1. "Create visualization" → "Tag cloud"
2. 필터: `level: "ERROR"`
3. Tags: `message.keyword`

---

## 실습 체크리스트

### Data View
- [ ] `application-logs-*` data view 생성 완료

### 시각화
- [ ] Log Level Distribution (파이 차트)
- [ ] Logs Over Time (라인 차트)
- [ ] Error Logs Over Time (영역 차트)
- [ ] Business Events (막대 차트)
- [ ] Top Errors (테이블)

### 대시보드
- [ ] Application Monitoring Dashboard 생성
- [ ] 모든 시각화 추가 및 배치

### 알림
- [ ] High Error Rate Alert 규칙 생성
- [ ] Business Event Failure Alert 규칙 생성

### 저장된 검색
- [ ] Error Logs 검색 저장
- [ ] Business Events 검색 저장
- [ ] User Created Events 검색 저장

---

## 트러블슈팅

### Data view가 보이지 않는 경우
```bash
# Elasticsearch 인덱스 확인
curl http://localhost:9200/_cat/indices?v

# application-logs-* 인덱스가 있는지 확인
# 없다면 Spring Boot 앱에서 API 호출하여 로그 생성
```

### 시각화가 비어있는 경우
- Time range를 "Last 15 minutes" 또는 "Last 1 hour"로 변경
- 필터가 너무 제한적이지 않은지 확인

### 알림이 작동하지 않는 경우
- Rule이 활성화되어 있는지 확인
- Query가 올바른지 Discover에서 테스트
- Threshold 값이 적절한지 확인

---

## 다음 단계
- 7단계: 전체 통합 테스트 및 문서화
- 실제 운영 환경을 위한 보안 설정 (Elasticsearch 인증, SSL 등)
