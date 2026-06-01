# Pinpoint 에이전트 연결 가이드 (다음 단계 — 앱 페이즈)

이 문서는 lab-redis 앱에 Pinpoint Java 에이전트를 붙이는 절차를 정리한다.
**현재 단계에서는 에이전트를 다운로드하거나 앱에 연결하지 않는다.**
백엔드 스택(`pinpoint-compose.yml`)이 정상 기동된 후 진행한다.

---

## 1. 에이전트 다운로드

Pinpoint 3.1.0 에이전트 tarball 을 GitHub Releases 에서 받는다.

```
URL 패턴:
https://github.com/pinpoint-apm/pinpoint/releases/download/v3.1.0/pinpoint-agent-3.1.0.tar.gz
```

편의 스크립트가 `docker/pinpoint/` 아래에 준비되어 있다:

- **Linux/macOS**: `download-agent.sh`
- **Windows (PowerShell)**: `download-agent.ps1`

스크립트를 실행하면 `docker/pinpoint/pinpoint-agent/` 아래에 압축이 풀린다.

```bash
# Linux/macOS
bash docker/pinpoint/download-agent.sh

# Windows PowerShell
.\docker\pinpoint\download-agent.ps1
```

---

## 2. JVM 에이전트 인자 템플릿

lab-redis 앱 기동 시 아래 JVM 인자를 추가한다:

```
-javaagent:/pinpoint-agent/pinpoint-bootstrap.jar
-Dpinpoint.agentId=lab-redis
-Dpinpoint.applicationName=lab-redis
-Dpinpoint.profiler.profiles.active=release
-Dprofiler.transport.grpc.collector.ip=<collector-host>
```

### `<collector-host>` 결정 방법

| 실행 환경 | collector-host 값 |
|-----------|-------------------|
| **컨테이너** (같은 `pinpoint-net` 네트워크) | `pinpoint-collector` (서비스명) |
| **호스트에서 직접 실행** (java -jar) | `host.docker.internal` (Windows/macOS) 또는 `172.24.0.30` (Linux) |

---

## 3. 컨테이너화 시 Dockerfile 예시

```dockerfile
FROM eclipse-temurin:21-jre

# 에이전트 파일을 이미지에 포함
COPY pinpoint-agent/ /pinpoint-agent/
COPY app.jar /app.jar

ENV JAVA_OPTS="\
  -javaagent:/pinpoint-agent/pinpoint-bootstrap.jar \
  -Dpinpoint.agentId=lab-redis \
  -Dpinpoint.applicationName=lab-redis \
  -Dpinpoint.profiler.profiles.active=release \
  -Dprofiler.transport.grpc.collector.ip=pinpoint-collector"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

lab-redis 컨테이너는 `pinpoint-net` 네트워크에 참여해야 Collector 와 통신할 수 있다:

```yaml
# lab-redis 앱 compose 에 추가
networks:
  pinpoint-net:
    external: true
    name: pinpoint-net
```

---

## 4. 네트워크 연결 확인

```bash
# Collector gRPC 포트 응답 확인 (호스트에서)
curl -v telnet://localhost:9991

# 컨테이너 내부에서
docker exec <app-container> curl -v telnet://pinpoint-collector:9991
```

트레이스가 수집되기 시작하면 Pinpoint Web UI(http://localhost:8088) 의
서버맵(ServerMap)과 에이전트 인스펙터(Inspector)에서 확인할 수 있다.

---

## 5. 포트 요약

| 포트 | 프로토콜 | 용도 |
|------|----------|------|
| 9991 | TCP (gRPC) | 에이전트 등록 |
| 9992 | TCP (gRPC) | 통계(Stat) 전송 |
| 9993 | TCP (gRPC) | 스팬(Span/트레이스) 전송 |
| 9994 | TCP | Thrift 기본 (레거시) |
| 9995 | TCP/UDP | Thrift Stat (레거시) |
| 9996 | TCP/UDP | Thrift Span (레거시) |

Java 21 에이전트는 기본적으로 gRPC(9991–9993)를 사용한다.
