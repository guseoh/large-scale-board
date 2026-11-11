# 런북: MySQL 중단

> 상태: **초안**. Wiki 페이지 `런북-MySQL-중단`으로 옮길 것. 근거: `docs/drill-mysql-draft.md` (2026-11-12 훈련).
> 알림 규칙의 `runbook` 주석이 이 페이지를 가리킨다.

## 증상

| 신호 | 언제 보이나 (주입 기준) | 어디서 |
| --- | --- | --- |
| **HikariTimeout** 알림 (critical) | 5~8초 | Prometheus Alerts (http://localhost:9090/alerts) |
| `/ping/db`, DB를 쓰는 API가 503 `DB_UNAVAILABLE` | 즉시, 약 1초 뒤 응답 | 응답 본문 `code`, 앱 로그 `ERROR [DB_UNAVAILABLE]` |
| **MySQLDown** 알림 (critical) | 20~23초 | Prometheus Alerts |
| HikariPending 알림 (warning) | 33~35초 | Prometheus Alerts |
| HighErrorRate 알림 (critical) | 70~73초 | Prometheus Alerts |

DB와 무관한 API(`/ping`)는 정상이어야 한다. 이것도 느려지면 아래 "번짐 확인"으로 간다.

## 확인할 패널과 쿼리

1. **Grafana `MySQL` 대시보드 → MySQL 상태:** `DOWN`이면 MySQL 쪽이다.
2. **Grafana `애플리케이션` 대시보드**
   - HikariCP 커넥션: 대기 스레드가 늘었는가.
   - 커넥션 획득 타임아웃: 0이 아닌가.
   - Tomcat 스레드: 사용 중이 최대(200)에 가까운가 (→ 번짐).
3. **Prometheus에서 직접**

   ```text
   mysql_up                                                         # 0이면 exporter도 접속 못 함
   up{job="mysql"}                                                  # 0이면 exporter 자체 문제
   increase(hikaricp_connections_timeout_total[1m])                 # 커넥션 획득 실패 건수
   tomcat_threads_busy_threads{application="board"}                 # 스레드 고갈 여부
   ```

4. **컨테이너 상태**

   ```text
   docker compose ps mysql            # Exited → stop/kill, Paused → pause, Up → 네트워크나 과부하
   docker compose logs mysql --tail 50
   ```

## 조치 순서

1. **상태에 맞게 되살린다**
   - `Exited`: `docker compose start mysql`. 강제 종료(kill) 뒤라면 로그에서 InnoDB 크래시 복구가 끝났는지 본다.
   - `Paused`: `docker compose unpause mysql`.
   - `Up`인데 응답 없음: `docker compose logs mysql`에서 원인(디스크, 메모리 한도 1.5GB 초과로 OOM 등)을 찾은 뒤 `docker compose restart mysql`.
2. **헬스 체크가 통과할 때까지 기다린다:** `docker compose ps mysql`이 `healthy`가 되면 연결을 받는다(훈련에서 0.4~5초).
3. **앱은 재시작하지 않는다.** HikariCP가 새 커넥션을 스스로 다시 만든다. 앱 재시작이 필요하면 다른 원인을 의심한다.

## 번짐 확인 (DB와 무관한 API까지 느려질 때)

- Tomcat 사용 중 스레드가 200에 닿았는가. 닿았다면 커넥션 대기 시간이 처리량에 비해 길다.
- 기준: 기다리는 요청 수 = 초당 요청 × `connection-timeout`. 이 값이 Tomcat 스레드(200)를 넘으면 번진다.
- 지금 설정(`connection-timeout` 1초)은 DB API가 초당 약 150건을 넘으면 다시 번질 수 있다. 처리량이 늘면 다시 계산한다.

## 복구 확인

- [ ] `mysql_up` = 1, MySQLDown 알림이 꺼짐 (복구 후 2~9초)
- [ ] `/ping/db`가 200을 돌려줌
- [ ] HikariCP 대기 스레드 0, 획득 타임아웃 증가가 멈춤
- [ ] HikariTimeout·HighErrorRate 알림은 1분 창이라 복구 후 약 1분 더 켜져 있다. 꺼질 때까지 기다린다.
- [ ] 장애 시각, 원인, 조치를 기록한다.

## 관련 설정

| 설정 | 값 | 이유 |
| --- | --- | --- |
| `spring.datasource.hikari.connection-timeout` | 1000ms | 스레드 고갈로 다른 API까지 번지는 것을 막는다 |
| JDBC `socketTimeout` | 10000ms | MySQL이 응답 없이 멈췄을 때 요청이 무한히 매달리지 않게 한다 |
| JDBC `connectTimeout` | 3000ms | 연결 자체가 안 될 때 빨리 포기한다 |
| `management.server.port` | 8082 | 요청 스레드가 고갈돼도 지표를 계속 수집한다 |
