/**
 * ============================================================
 * catalog-load-test.js — Redis 타임아웃 원인 판별 부하 테스트
 * ============================================================
 *
 * ## 실험 목적
 *   2-CPU 제한 컨테이너(lab-redis-app)에서 Jackson 역직렬화(596KB)가
 *   Lettuce EventLoop를 굶겨 RedisCommandTimeoutException → HTTP 500을
 *   유발하는지 확인하고, L1(Caffeine) 캐시가 이를 해소하는지 검증한다.
 *
 * ## 실행 모드
 *
 *   [1] 재현 — 타임아웃 유발 (L1 캐시 OFF, 대용량 객체, 고부하)
 *       예상 결과: CPU 포화 → Lettuce EventLoop 지연 → HTTP 500 다수 발생
 *
 *       docker run --rm \
 *         --add-host host.docker.internal:host-gateway \
 *         -e BASE_URL=http://host.docker.internal:8080 \
 *         -e L1=off \
 *         -e SIZE=large \
 *         -v "${PWD}/k6:/scripts" \
 *         grafana/k6 run /scripts/catalog-load-test.js
 *
 *   [2] 해결 — 타임아웃 소멸 (L1 캐시 ON, 동일 부하)
 *       예상 결과: Caffeine 캐시 히트 → 역직렬화 생략 → CPU 안정 → 500 없음
 *
 *       docker run --rm \
 *         --add-host host.docker.internal:host-gateway \
 *         -e BASE_URL=http://host.docker.internal:8080 \
 *         -e L1=on \
 *         -e SIZE=large \
 *         -v "${PWD}/k6:/scripts" \
 *         grafana/k6 run /scripts/catalog-load-test.js
 *
 * ## Prometheus remote-write로 Grafana 실시간 확인 (선택)
 *       docker run --rm \
 *         --add-host host.docker.internal:host-gateway \
 *         -e BASE_URL=http://host.docker.internal:8080 \
 *         -e L1=off \
 *         -e SIZE=large \
 *         -e K6_PROMETHEUS_RW_SERVER_URL=http://host.docker.internal:9090/api/v1/write \
 *         -v "${PWD}/k6:/scripts" \
 *         grafana/k6 run -o experimental-prometheus-rw /scripts/catalog-load-test.js
 *
 * ## 환경 변수
 *   BASE_URL  — 앱 베이스 URL (기본값: http://localhost:8080)
 *   SIZE      — 카탈로그 크기: large | small (기본값: large)
 *   L1        — Caffeine L1 캐시 여부: on | off (기본값: off)
 * ============================================================
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate } from "k6/metrics";

// ─── 환경 변수 ────────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SIZE     = __ENV.SIZE     || "large";
const L1       = __ENV.L1      || "off";

// ─── 커스텀 메트릭 ─────────────────────────────────────────────────────────────
// HTTP 500 응답 횟수 (= Redis 타임아웃 발생 건수)
const timeoutCount = new Counter("catalog_timeout_total");
// 요청 성공률 (status === 200)
const requestOkRate = new Rate("catalog_request_ok_rate");

// ─── 테스트 옵션 ────────────────────────────────────────────────────────────────
export const options = {
  // ── 부하 단계 (2-CPU 앱을 포화시키기 위해 VU를 공격적으로 올림) ──────────────
  // 수정이 필요하면 아래 stages 배열만 편집한다.
  stages: [
    { duration: "30s", target: 50  },  // 0→50 VU  : 워밍업
    { duration: "1m",  target: 150 },  // →150 VU   : 중간 부하
    { duration: "2m",  target: 300 },  // →300 VU   : 2-CPU 포화 구간 (핵심)
    { duration: "30s", target: 0   },  // →0 VU     : 램프다운
  ],

  // ── 임계값 (abortOnFail 미설정 → 실패해도 실행 계속, 최종 요약에만 반영) ───
  // 실험 목적상 500 에러를 관찰해야 하므로 rate<1.0 으로 절대 중단되지 않게 설정.
  thresholds: {
    http_req_failed:        ["rate<1.0"],   // 100% 이하이면 통과 (사실상 항상 통과)
    http_req_duration:      ["p(95)<10000"], // p95 < 10s (관찰용, 중단 없음)
    catalog_request_ok_rate: ["rate>0.0"],   // 최소 1건 이상 성공 (기본 동작 확인용)
  },
};

// ─── 가상 유저 시나리오 ─────────────────────────────────────────────────────────
export default function () {
  const url = `${BASE_URL}/api/catalog?size=${SIZE}&l1=${L1}`;

  const res = http.get(url, {
    tags: { endpoint: "catalog", l1: L1, size: SIZE },
  });

  // 200 여부 체크
  const ok = check(res, {
    "status is 200": (r) => r.status === 200,
  });

  // 커스텀 메트릭 기록
  requestOkRate.add(ok);
  if (res.status === 500) {
    timeoutCount.add(1);
  }

  // 짧은 대기 (EventLoop 과부하 실험이므로 think time 최소화)
  sleep(0.05);
}
