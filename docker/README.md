# Docker 스택 가이드

이 디렉터리에는 인프라 실험 랩에서 사용하는 세 개의 Compose 스택이 있다.

---

## 포트 맵

| 서비스 | 호스트 포트 | 용도 |
|--------|------------|------|
| **Pinpoint Web UI** | **8088** | APM 트레이스 / 서버맵 / 인스펙터 |
| Grafana | 3000 | 시계열 대시보드 |
| Prometheus | 9090 | 메트릭 수집 |
| Redis | 6379 | 실험 대상 Redis |
| redis_exporter | 9121 | Redis → Prometheus 브릿지 |
| Pinpoint Collector gRPC | 9991 / 9992 / 9993 | 에이전트 → Collector |
| Pinpoint Collector Thrift | 9994 / 9995 / 9996 | Collector (레거시) |
| Pinpoint HBase | 16010 | HBase Master WebUI (내부 확인용) |
| Pinpoint Zookeeper | 2181 | ZK 클라이언트 포트 |
| *(lab-redis 앱, 다음 단계)* | 8080 | Spring Boot 앱 (Pinpoint Web 과 충돌 방지용 예약) |

> Pinpoint Web 이 **8080 대신 8088** 을 사용하는 이유: lab-redis 앱이 8080 을 사용하기 때문.

---

## 스택별 기동 명령

### 1. Pinpoint APM 백엔드

```bash
docker compose -f docker/pinpoint-compose.yml up -d
```

> ⚠ **RAM 경고**: HBase + Zookeeper + Collector + Web 합산 **6–8 GB 이상** 필요.  
> Docker Desktop → Settings → Resources 에서 메모리 할당을 확인할 것.  
> 최초 기동 시 HBase 스키마 초기화에 **1–2분** 소요됨.  
> HBase 준비 전에 Web UI 에 접속하면 연결 오류가 표시된다 — 로그 확인 후 접속.

### 2. Prometheus + Grafana

```bash
docker compose -f docker/monitoring-compose.yml up -d
```

### 3. Redis + redis_exporter

```bash
docker compose -f docker/redis-compose.yml up -d
```

---

## 동작 확인

### Pinpoint Web UI
```
http://localhost:8088
```
에이전트가 연결된 앱이 없으면 서버맵이 비어 있다.
- 트레이스 동작을 미리 확인하려면 `pinpoint-compose.yml` 하단의
  `pinpoint-quickstart` 주석을 해제하고 다시 기동.
- lab-redis 앱(다음 단계)에 에이전트가 붙으면 자동으로 트레이스가 표시된다.

### Grafana
```
http://localhost:3000  (admin / admin)
```

### Prometheus
```
http://localhost:9090
```

---

## 에이전트 연결 (다음 단계)

앱에 Pinpoint 에이전트를 붙이는 절차는 `docker/pinpoint/README.md` 참조.  
앱 컨테이너와 Collector 간 네트워크 연결은 앱 페이즈에서 확정된다.

---

## 전체 종료

```bash
docker compose -f docker/pinpoint-compose.yml down
docker compose -f docker/monitoring-compose.yml down
docker compose -f docker/redis-compose.yml down
```

데이터 볼륨까지 삭제하려면 `-v` 플래그 추가 (`down -v`).
