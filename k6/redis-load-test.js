/**
 * k6 load test for lab-redis endpoints.
 *
 * 참고: 도메인 코드(Controller/Service)가 추가된 후 사용하세요.
 *       현재 구조는 apps/modules/supports 레이어 뼈대만 갖추고 있습니다.
 *
 * Targets:
 *   POST /api/redis/{key}?value=<val>   — write a key
 *   GET  /api/redis/{key}               — read a key
 *
 * Run (plain, no metrics export):
 *   k6 run redis-load-test.js
 *
 * Run with Prometheus remote-write output (recommended for Grafana dashboards):
 *   K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
 *     k6 run -o experimental-prometheus-rw redis-load-test.js
 *
 * The Prometheus instance in docker/monitoring-compose.yml must be started
 * with --web.enable-remote-write-receiver (already configured in the compose file).
 */

import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

// ─── custom metrics ───────────────────────────────────────────────────────────
const errorRate = new Rate("redis_error_rate");

// ─── test options ─────────────────────────────────────────────────────────────
export const options = {
  stages: [
    { duration: "30s", target: 10 },  // ramp-up:   0 → 10 VUs over 30 s
    { duration: "1m",  target: 10 },  // steady:    hold 10 VUs for 1 min
    { duration: "30s", target: 50 },  // ramp-up:   10 → 50 VUs over 30 s
    { duration: "2m",  target: 50 },  // steady:    hold 50 VUs for 2 min
    { duration: "30s", target: 0  },  // ramp-down: 50 → 0 VUs over 30 s
  ],
  thresholds: {
    http_req_failed:  ["rate<0.01"],   // <1 % errors
    http_req_duration: ["p(95)<500"],  // 95th percentile < 500 ms
    redis_error_rate:  ["rate<0.01"],
  },
};

// ─── base URL ─────────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

// ─── helpers ──────────────────────────────────────────────────────────────────
function randomKey() {
  return `k6-key-${Math.floor(Math.random() * 1000)}`;
}

// ─── virtual user scenario ────────────────────────────────────────────────────
export default function () {
  const key   = randomKey();
  const value = `value-${Date.now()}`;

  // ── write ──
  const writeRes = http.post(
    `${BASE_URL}/api/redis/${key}?value=${encodeURIComponent(value)}`,
  );
  const writeOk = check(writeRes, {
    "write: status 200":           (r) => r.status === 200,
    "write: body contains success": (r) => r.body && r.body.includes('"success":true'),
  });
  errorRate.add(!writeOk);

  sleep(0.1);

  // ── read ──
  const readRes = http.get(`${BASE_URL}/api/redis/${key}`);
  const readOk = check(readRes, {
    "read: status 200":           (r) => r.status === 200,
    "read: body contains value":  (r) => r.body && r.body.includes(value),
  });
  errorRate.add(!readOk);

  sleep(0.1);
}
