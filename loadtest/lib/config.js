// 모든 시나리오가 공유하는 설정. 값은 환경 변수(-e 이름=값)로 덮어쓴다.

// k6는 컨테이너에서, 앱은 호스트(bootRun)에서 돈다. compose.yaml의 BASE_URL 기본값과 같다.
export const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';

export function envInt(name, defaultValue) {
  const raw = __ENV[name];
  if (raw === undefined || raw === '') {
    return defaultValue;
  }
  const value = parseInt(raw, 10);
  if (Number.isNaN(value)) {
    throw new Error(`${name}은 정수여야 합니다: ${raw}`);
  }
  return value;
}

/**
 * 도착률 고정(open model) 실행기 설정. (D0-15)
 *
 * VU 수 고정(closed model)은 서버가 느려지면 요청도 덜 보내서 지연이 실제보다 좋게 측정된다.
 * 도착률 고정은 서버 상태와 무관하게 정해진 속도로 보내므로, 과부하에서 큐가 쌓이는 모습이
 * 그대로 보인다.
 *
 * MODE=constant  RATE(초당 요청)로 DURATION 동안 보낸다.
 *                표준 측정(워밍업 1분 + 측정 3분 x 3회)에 쓴다.
 * MODE=step      START_RATE에서 STEP_RATE씩 STEPS번 올린다. 계단마다 STEP_DURATION.
 *                최대 처리량을 찾을 때 쓴다.
 */
export function arrivalRateScenario() {
  const mode = __ENV.MODE || 'constant';
  // 응답이 느려지면 동시에 떠 있는 요청이 늘어난다.
  // VU가 모자라면 k6가 요청을 버리고(dropped_iterations) 경고한다.
  const preAllocatedVUs = envInt('PRE_VUS', 50);
  const maxVUs = envInt('MAX_VUS', 500);

  if (mode === 'constant') {
    return {
      executor: 'constant-arrival-rate',
      rate: envInt('RATE', 100),
      timeUnit: '1s',
      duration: __ENV.DURATION || '1m',
      preAllocatedVUs,
      maxVUs,
    };
  }
  if (mode === 'step') {
    const start = envInt('START_RATE', 500);
    const step = envInt('STEP_RATE', 500);
    const steps = envInt('STEPS', 8);
    const stepDuration = __ENV.STEP_DURATION || '30s';
    const stages = [];
    for (let i = 0; i < steps; i++) {
      const target = start + step * i;
      stages.push({ target, duration: '5s' });         // 다음 계단으로 올라간다
      stages.push({ target, duration: stepDuration }); // 계단에 머문다
    }
    return {
      executor: 'ramping-arrival-rate',
      startRate: start,
      timeUnit: '1s',
      stages,
      preAllocatedVUs,
      maxVUs,
    };
  }
  throw new Error(`MODE는 constant 또는 step이어야 합니다: ${mode}`);
}
