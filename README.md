# infrastructure-lab

Redis 부하 테스트 및 클린 아키텍처 학습을 위한 Gradle 멀티 모듈 Java 프로젝트.

각 앱은 **apps / modules / supports** 3계층 모듈 구조와 클린 아키텍처(interfaces → application → domain ← infrastructure)를 따른다.
공유 Prometheus + Grafana 스택이 전체 관찰 가능성을 제공한다.

> **컨벤션:** `ApiResponse`, `CoreException`, `ErrorType` 등 공유 계약 클래스는 중앙 모듈에 두지 않고 각 앱 내부(`interfaces/api`, `support/error` 패키지)에 직접 포함한다. 앱별 복사 방식으로 참고 프로젝트(commerce-api / pg-simulator) 와 동일한 컨벤션을 따른다.

---

## 모듈 구조

```
infrastructure-lab/
├── apps/
│   └── lab-redis/          # Redis 연동 Spring Boot 앱 (BootJar 생성)
├── modules/
│   └── redis/              # 재사용 가능한 Redis 설정 모듈 (StringRedisTemplate 빈 제공)
├── supports/
│   └── monitoring/         # Actuator + Prometheus 모니터링 애드온
├── docker/
│   ├── redis-compose.yml       # Redis + redis_exporter
│   ├── monitoring-compose.yml  # Prometheus + Grafana
│   ├── prometheus.yml          # 스크레이프 설정
│   └── grafana/provisioning/   # Grafana 데이터소스 프로비저닝
└── k6/
    └── redis-load-test.js  # k6 부하 테스트 스크립트 (도메인 추가 후 사용)
```

### 레이어 의존 방향

```
interfaces → application → domain ← infrastructure
```

| 레이어 | 패키지 | 책임 |
|--------|--------|------|
| interfaces | `io.github.tsseo.interfaces` | Controller, Request/Response DTO |
| application | `io.github.tsseo.application` | Facade (유스케이스 조합), Criteria/Result |
| domain | `io.github.tsseo.domain` | Entity, Service, Repository 인터페이스(포트) |
| infrastructure | `io.github.tsseo.infrastructure` | Repository 구현체, 외부 연동 어댑터 |

---

## 실행 방법

### 1. Redis + redis-exporter 시작

```bash
docker compose -f docker/redis-compose.yml up -d
```

- Redis 7 (Alpine) — 포트 **6379**
- `oliver006/redis_exporter` — 포트 **9121** (Prometheus 가 스크레이프)

### 2. Prometheus + Grafana 시작

```bash
docker compose -f docker/monitoring-compose.yml up -d
```

- **Prometheus** — [http://localhost:9090](http://localhost:9090)
  - `apps/lab-redis` 앱의 `/actuator/prometheus` 스크레이프 (`host.docker.internal:8080`)
  - `redis_exporter` 스크레이프 (`host.docker.internal:9121`)
  - k6 remote-write receiver 활성화
- **Grafana** — [http://localhost:3000](http://localhost:3000) (기본 계정: `admin` / `admin`)
  - Prometheus 데이터소스 자동 프로비저닝

### 3. lab-redis 앱 시작

```bash
./gradlew :apps:lab-redis:bootRun
```

앱은 **http://localhost:8080** 에서 시작된다.

> **참고:** 현재는 구조 뼈대만 구성되어 있으므로 도메인 코드(Controller/Service)를 추가한 후 엔드포인트를 사용할 수 있다.

| 경로 | 설명 |
|------|------|
| `GET /actuator/health` | 헬스 체크 |
| `GET /actuator/prometheus` | Micrometer / Prometheus 메트릭 |

### 4. k6 부하 테스트 실행

> 도메인 Controller 가 추가된 후 사용하세요.

**기본 실행** (터미널 출력만):
```bash
k6 run k6/redis-load-test.js
```

**Prometheus remote-write 연동** (Grafana 에서 메트릭 확인):
```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
  k6 run -o experimental-prometheus-rw k6/redis-load-test.js
```

### 5. Grafana 대시보드

- Grafana: [http://localhost:3000](http://localhost:3000)
- Prometheus: [http://localhost:9090](http://localhost:9090)

추천 대시보드 (Grafana 에서 Import):
- Redis: Dashboard ID **763** (redis_exporter)
- Spring Boot: Dashboard ID **17175** 또는 **4701**

---

## 스택 버전

| 컴포넌트 | 버전 |
|---------|------|
| Java | 21 |
| Gradle | 9.3.0 |
| Spring Boot | 3.4.1 |
| Spring Dependency Management | 1.1.7 |
| Redis 이미지 | redis:7-alpine |
| redis_exporter | oliver006/redis_exporter:latest |
| Prometheus | prom/prometheus:latest |
| Grafana | grafana/grafana:latest |

---

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `REDIS_HOST` | `localhost` | Redis 호스트명 |
| `REDIS_PORT` | `6379` | Redis 포트 |
| `BASE_URL` | `http://localhost:8080` | k6 대상 베이스 URL |

---

## Gradle 빌드 명령

```bash
# 프로젝트 목록 확인
./gradlew projects

# 전체 빌드
./gradlew clean build

# 앱 실행
./gradlew :apps:lab-redis:bootRun

# 특정 모듈 빌드
./gradlew :modules:redis:build
```
