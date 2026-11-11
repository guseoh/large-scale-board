// 장애 훈련: MySQL 중단 (Step-0-Design 13절)
// /ping(DB 없음)과 /ping/db에 각각 초당 RATE건을 동시에 보낸다.
// 장애가 DB와 무관한 API까지 번지는지 본다.
// 직접 실행하지 않고 scripts/drill-mysql.ps1이 장애 주입과 함께 실행한다.
//
// 장애를 일부러 일으키므로 통과 기준(thresholds)을 두지 않는다.
// 결과는 Grafana와 Prometheus에서 본다.
import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, envInt } from '../lib/config.js';

const RATE = envInt('RATE', 100);
const DURATION = __ENV.DURATION || '5m';

// 요청이 30초씩 매달리면 동시에 떠 있는 요청이 초당 100건 x 30초 = 3,000개까지 늘 수 있다.
// VU 하나가 메모리를 쓰므로(컨테이너 한도 1GB) 상한을 둔다. 상한에 닿으면 k6가 요청을 버리고
// dropped_iterations로 기록한다. 이것도 "클라이언트가 보내지 못한 요청"이라는 장애 증상이다.
const MAX_VUS = envInt('MAX_VUS', 600);

function scenario(exec) {
  return {
    executor: 'constant-arrival-rate',
    exec,
    rate: RATE,
    timeUnit: '1s',
    duration: DURATION,
    preAllocatedVUs: 50,
    maxVUs: MAX_VUS,
  };
}

export const options = {
  scenarios: {
    ping: scenario('ping'),
    pingdb: scenario('pingDb'),
  },
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max'],
};

export function ping() {
  const res = http.get(`${BASE_URL}/api/v1/ping`, {
    tags: { name: '/api/v1/ping' },
    timeout: '60s',
  });
  check(res, { 'status 200': (r) => r.status === 200 });
}

export function pingDb() {
  const res = http.get(`${BASE_URL}/api/v1/ping/db`, {
    tags: { name: '/api/v1/ping/db' },
    timeout: '60s',
  });
  check(res, { 'status 200': (r) => r.status === 200 });
}
