# infrastructure-lab

Redis, Kafka, RabbitMQ 등 여러 인프라를 직접 붙여 **부하 테스트하고 학습**하는 멀티 모듈 프로젝트.
인프라별로 독립된 앱으로 분리해 서로 간섭 없이 실험한다.

## 구조

`apps / modules / supports` 3계층 멀티 모듈 + **4-레이어드 아키텍처**(`interfaces → application → domain ← infrastructure`).
Repository를 도메인 인터페이스(포트) / 인프라 구현체로 분리해 **의존성 역전(DIP)** 을 적용한다.

```
apps/        # 인프라별 실행 앱 (lab-redis, lab-kafka, ...)
modules/     # 재사용 설정 모듈 (redis, kafka, ...)
supports/    # 공통 애드온 (monitoring 등)
docker/      # Redis / Prometheus / Grafana 로컬 인프라
k6/          # 부하 테스트 스크립트
```

## 인프라별 진행 상황

| 인프라 | 앱 | 상태 |
|--------|-----|------|
| Redis | `apps/lab-redis` | 구조 완료 (도메인 예정) |
| Kafka | `apps/lab-kafka` | 예정 |
| RabbitMQ | `apps/lab-rabbitmq` | 예정 |

## 빠른 시작

```bash
# 로컬 인프라 + 모니터링
docker compose -f docker/redis-compose.yml up -d
docker compose -f docker/monitoring-compose.yml up -d

# 앱 실행
./gradlew :apps:lab-redis:bootRun
```

- 앱: http://localhost:8080 (`/actuator/prometheus` 노출)
- Prometheus: http://localhost:9090 · Grafana: http://localhost:3000 (admin/admin)

## 스택

Java 21 · Gradle 9.3 · Spring Boot 3.4.1 · k6 · Prometheus + Grafana
