// 기준선 측정: /api/v1/ping(앱만) 또는 /api/v1/ping/db(커넥션 풀 + SELECT 1). (Step-0-Design 10절)
//
// 실행 예 (PowerShell, 저장소 루트):
//   docker compose --profile loadtest run --rm k6 run --tag testid=ping-100 -e RATE=100 -e DURATION=1m /scripts/scenarios/ping.js
//   docker compose --profile loadtest run --rm k6 run --tag testid=pingdb-step -e TARGET=db -e MODE=step /scripts/scenarios/ping.js
//
// 실험 기록에 남길 것: 데이터셋 버전, 커밋 해시, 도착률, 풀·스레드 설정, 접근 로그 사용
// 여부(board.logging.access-enabled).
import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, arrivalRateScenario } from '../lib/config.js';

const TARGET = __ENV.TARGET || 'app'; // app | db
const PATH = TARGET === 'db' ? '/api/v1/ping/db' : '/api/v1/ping';

export const options = {
  scenarios: {
    ping: arrivalRateScenario(),
  },
  thresholds: {
    // 통과 기준: 실패율 1% 미만. p99 임계값은 첫 측정 후 정한다.
    http_req_failed: ['rate<0.01'],
  },
  // 요약에 p99를 포함한다(기본은 p90, p95).
  summaryTrendStats: ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const res = http.get(`${BASE_URL}${PATH}`, { tags: { name: PATH } });
  check(res, { 'status 200': (r) => r.status === 200 });
}
