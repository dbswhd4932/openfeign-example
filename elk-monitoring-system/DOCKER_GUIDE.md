# ELK Stack Docker 환경 가이드

## 개요
이 프로젝트는 Docker Compose를 사용하여 ELK Stack (Elasticsearch, Logstash, Kibana)과 MySQL을 구성합니다.

## 구성 요소

### 1. MySQL (포트: 3306)
- **용도**: 애플리케이션 데이터베이스
- **접속 정보**:
  - Host: localhost:3306
  - Database: elk_monitoring
  - Username: root
  - Password: root1234

### 2. Elasticsearch (포트: 9200, 9300)
- **용도**: 로그 저장 및 검색 엔진
- **접속**: http://localhost:9200
- **특징**:
  - Single-node 모드
  - 보안 비활성화 (학습용)
  - 메모리: 512MB

### 3. Logstash (포트: 5000, 9600)
- **용도**: 로그 수집 및 변환
- **입력**:
  - TCP 5000번 포트: Spring Boot 애플리케이션의 JSON 로그
  - File: /logs/application.log
- **출력**: Elasticsearch

### 4. Kibana (포트: 5601)
- **용도**: 로그 시각화 및 대시보드
- **접속**: http://localhost:5601

## Docker 명령어

### 전체 서비스 시작
```bash
docker-compose up -d
```

### 특정 서비스만 시작
```bash
docker-compose up -d mysql
docker-compose up -d elasticsearch
docker-compose up -d logstash
docker-compose up -d kibana
```

### 서비스 상태 확인
```bash
docker-compose ps
```

### 로그 확인
```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs elasticsearch
docker-compose logs logstash
docker-compose logs kibana
docker-compose logs mysql

# 실시간 로그 모니터링
docker-compose logs -f logstash
```

### 서비스 중지
```bash
docker-compose down
```

### 서비스 중지 및 볼륨 삭제 (데이터 초기화)
```bash
docker-compose down -v
```

### 서비스 재시작
```bash
docker-compose restart
```

## 서비스 헬스 체크

### Elasticsearch
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### MySQL
```bash
docker exec -it elk-mysql mysql -uroot -proot1234 -e "SELECT 1"
```

### Logstash
```bash
curl http://localhost:9600/_node/stats?pretty
```

### Kibana
브라우저에서 http://localhost:5601 접속

## 주요 학습 포인트

### 1. Docker Compose 기본 개념
- **services**: 각 컨테이너 정의
- **networks**: 컨테이너 간 통신
- **volumes**: 데이터 영구 저장
- **depends_on**: 서비스 시작 순서 제어
- **healthcheck**: 서비스 상태 확인

### 2. ELK Stack 데이터 흐름
```
Spring Boot App → Logstash (5000) → Elasticsearch (9200) → Kibana (5601)
```

### 3. 볼륨 관리
- `mysql-data`: MySQL 데이터 영구 저장
- `elasticsearch-data`: Elasticsearch 인덱스 데이터 저장
- `./logs`: 애플리케이션 로그 파일

### 4. 네트워크
- `elk-network`: 모든 서비스가 같은 네트워크에서 통신

## 트러블슈팅

### Elasticsearch가 시작되지 않는 경우
```bash
# 메모리 부족일 수 있음
docker-compose logs elasticsearch

# vm.max_map_count 설정 (Linux/Mac)
sudo sysctl -w vm.max_map_count=262144
```

### Logstash가 Elasticsearch에 연결되지 않는 경우
```bash
# Elasticsearch가 완전히 시작될 때까지 대기
docker-compose logs elasticsearch | grep "started"

# Logstash 재시작
docker-compose restart logstash
```

### 포트 충돌
```bash
# 사용 중인 포트 확인
lsof -i :3306
lsof -i :9200
lsof -i :5601

# 프로세스 종료 후 재시작
docker-compose down
docker-compose up -d
```

## 다음 단계
- 3단계: Spring Boot 애플리케이션에 Logback 설정
- 4단계: REST API 및 예제 코드 작성
