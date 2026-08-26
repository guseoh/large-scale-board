# large-scale-board 프로젝트 인수인계 및 학습 운영 기준

> 기준일: 2026-08-26
>
> 저장소: `guseoh/large-scale-board`
>
> 이 문서는 새 ChatGPT 세션이나 새로운 학습 세션이 기존 대화 맥락 없이도 프로젝트를 즉시 이어갈 수 있도록 만든 **프로젝트 인수인계 기준 문서**다.
>
> 전체 세부 로드맵은 `README.md`가 최우선 기준이며, 이 문서는 프로젝트의 방향성, 목표, 운영 규칙, 현재 베이스라인, Git/Issue/PR 사용 방식과 세션 인수인계 원칙을 함께 정리한다.

---

# 1. 프로젝트의 방향성과 목표

`large-scale-board`는 완성된 게시판 서비스를 빠르게 만드는 프로젝트가 아니다.

비교적 단순한 Spring Boot + MySQL 기반 게시판에서 출발해 데이터와 트래픽을 점진적으로 증가시키고, 그 과정에서 발생하는 병목, 동시성, 데이터 정합성, 장애, 분산 시스템 문제를 직접 재현한 뒤 여러 해결 전략을 비교하며 시스템을 발전시키는 **대규모 시스템 및 분산 시스템 설계 학습 프로젝트**다.

핵심 목표는 Redis, Kafka, MSA, CQRS 같은 기술을 많이 사용하는 것이 아니다. 각 기술이 없을 때 어떤 문제가 생기는지 먼저 경험하고, 여러 해결 후보를 비교한 뒤 코드와 실험으로 선택을 검증하는 것이 핵심이다.

최종적으로 다음 질문에 코드와 측정 결과를 근거로 답할 수 있는 수준을 목표로 한다.

```text
현재 단순 구조에서는 어떤 문제가 발생하는가?
왜 지금 이 문제가 발생하는가?
어디가 실제 병목 또는 실패 지점인가?
어떤 해결 후보가 있는가?
각 후보는 어떤 비용과 새로운 실패 가능성을 만드는가?
어떤 근거로 하나를 선택하는가?
동일한 조건에서 개선 효과를 측정할 수 있는가?
장애 상황에서도 데이터 정확성·유실·중복·지연을 설명할 수 있는가?
```

최종 아키텍처 자체보다 **단순한 구조가 왜 점점 복잡한 구조로 발전했는지 설명할 수 있는 과정**을 더 중요하게 본다.

---

# 2. 학습 운영 원칙

모든 주요 학습 단위는 가능한 한 다음 흐름을 따른다.

```text
문제 정의
→ 단순 구현
→ 문제 재현
→ 측정
→ 원인 분석
→ 해결 후보 비교
→ 구현
→ 동일 조건 재측정
→ 장애 실험
→ Trade-off 해석
```

추가 원칙:

- 성능 개선 전에는 반드시 현재 상태를 먼저 측정한다.
- 기술을 로드맵에 적혀 있다는 이유만으로 미리 도입하지 않는다.
- 가능한 경우 하나의 문제에 여러 해결 방법을 적용해 직접 비교한다.
- 정상 상황뿐 아니라 과부하, 장애, 데이터 유실, 중복, 지연을 적극적으로 만든다.
- 평균 응답 시간만 보지 않고 처리량, p50, p95, p99, 오류율, DB Lock Wait, Consumer Lag, 커넥션 풀 대기 등 문제에 맞는 지표를 함께 본다.
- 하나의 실험에서는 가능한 한 하나의 변수만 변경한다.
- Before/After는 같은 데이터와 같은 부하 조건에서 비교한다.
- 실패한 실험도 학습 결과로 취급한다.
- 포트폴리오 프로젝트처럼 기술 수를 최소화할 필요는 없다. 이 프로젝트는 학습용이므로 데이터베이스, 캐시, 메시징, 관측, 인프라, 운영 기술을 폭넓게 경험할 수 있다.
- 다만 모든 기술은 `왜 필요한가`, `어떻게 동작하는가`, `대안은 무엇인가`, `무슨 비용이 추가되는가`까지 설명할 수 있어야 한다.
- 모놀리스에서 경험한 문제를 이후 분산 시스템에서 다시 경험하고, 같은 기술이나 문제의 성격이 어떻게 달라지는지 비교한다.

---

# 3. 참고 강의와 외부 자료 사용 방식

참고 자료:

- 인프런 공개 강의 페이지: `스프링부트로 직접 만들면서 배우는 대규모 시스템 설계 - 게시판`

이 강의는 구매하지 않았으며, **공개된 목차에서 다루는 기술과 주제만 참고한다.**

따라서 기준은 다음과 같다.

```text
README 로드맵
= 프로젝트의 실제 학습 순서와 진행 기준

인프런 공개 목차
= MySQL / Redis / Kafka / 동시성 / 분산 시스템 등
  관련 기술 범위를 확인하는 참고 자료
```

강의 섹션 순서나 구현 코드를 그대로 따라가는 프로젝트가 아니다.

필요한 주제에서는 공식 문서, 기술 서적, 한국 IT 기업 기술 블로그, 일반 기술 블로그, 오픈소스 프로젝트와 실험 사례도 적극적으로 참고한다.

UNBOX 같은 복잡한 분산 시스템 프로젝트도 구현 아이디어와 실험 방식을 참고할 수 있지만 그대로 복제하지 않는다.

---

# 4. 전체 로드맵 구조

전체 로드맵의 세부 항목과 완료 조건은 반드시 `README.md`를 기준으로 한다.

## Part 1 — 대규모 단일 시스템

하나의 Spring Boot 애플리케이션과 데이터베이스에서 시작한다.

```text
Spring Boot + MySQL
→ 대규모 데이터
→ DB 병목과 조회 최적화
→ Replication / Sharding
→ 동시성
→ Redis
→ Kafka
→ Transactional Outbox
→ CQRS / Read Model
→ Cache
→ 장애 및 성능 실험
```

### Phase 0 — 시스템 기반과 개발 환경

- Scale-up / Scale-out
- Load Balancer
- Stateful / Stateless
- Monolith / MSA
- Docker / Docker Compose
- MySQL
- Spring Boot
- JPA
- Testcontainers
- Actuator / Micrometer
- JVM / HTTP / HikariCP 기본 관측

### Phase 1 — 게시글과 대규모 데이터베이스

```text
단일 DB
→ 대량 데이터
→ 병목 측정
→ Index / Query 개선
→ 단일 DB 한계 확인
→ Replication
→ Sharding
```

주요 범위:

- 대량 데이터
- Auto Increment / UUID / Snowflake ID
- OFFSET / Cursor Pagination
- Index / `EXPLAIN ANALYZE`
- Slow Query
- Primary / Replica와 Read Scaling
- 복제 지연
- Sharding
- Shard Key
- Hot Shard
- Cross-Shard Query
- 일부 Shard 장애

### Phase 2 — 계층형 댓글

- Adjacency List
- Path Enumeration
- Materialized Path
- Closure Table
- 계층 조회 / 정렬 / 삭제
- 깊은 트리 / 넓은 트리
- 모델별 읽기·쓰기 비용 비교

### Phase 3 — 좋아요와 동시성

주요 비교:

```text
Atomic Update
vs
Optimistic Lock
vs
Pessimistic Lock
vs
Redis Distributed Lock
```

추가 범위:

- DB Constraint
- Lost Update
- 멱등성
- 재시도
- Local Lock 한계
- Multi-instance 동시성
- Redis Lua 원자 처리
- 처리량 / 충돌 / 재시도 / p95 / p99 / DB Lock Wait 비교

### Phase 4 — 조회수와 높은 쓰기 트래픽

```text
MySQL UPDATE
→ Hot Row
→ Lock 경합
→ Redis INCR
→ Write-back
→ Batch Flush
```

성능뿐 아니라 Redis 장애, 반영 지연, 애플리케이션 종료와 데이터 유실 범위까지 확인한다.

### Phase 5 — 인기글과 Event Driven Architecture

- Kafka Topic / Partition / Offset / Consumer Group
- Ordering
- At-least-once
- Duplicate Event
- Idempotent Consumer
- Retry / Backoff
- DLQ
- Consumer Lag
- Producer / Consumer 장애
- DB Commit 성공 + Kafka Publish 실패 재현
- Transactional Outbox
- Outbox Relay
- CDC / Transaction Log Tailing
- 이벤트 적체와 복구

### Phase 6 — 게시글 조회 최적화

```text
기존 DB 조회
→ 복잡한 Join / Query
→ CQRS Read Model
→ Eventual Consistency
→ Cache Aside
→ Cache Stampede
→ Hot Key
→ Request Collapsing
```

CQRS는 클래스 이름 분리가 아니라 Write Model과 Read Model을 실제 데이터 구조 수준에서 분리하는 것을 목표로 한다.

### Phase 7 — 전체 시스템 통합과 검증

Part 1 전체에서 다음을 다시 검증한다.

- MySQL 장애
- Redis 장애
- Kafka 장애
- Consumer 중단
- Outbox Relay 중단
- Duplicate Event
- Event Delay
- Read Model Delay
- Cache / DB 불일치
- Shard 일부 장애
- 전체 k6 부하 테스트
- Grafana 통합 관측

## Part 2 — 분산 시스템

Part 1을 충분히 경험한 뒤 기존 시스템을 실제 분산 시스템으로 발전시킨다.

```text
도메인 경계 분석
→ Modular Monolith
→ 일부 서비스 실제 분리
→ Database per Service
→ 서비스 간 동기 통신
→ 네트워크 장애와 장애 전파
→ 비동기 이벤트 통신
→ 분산 데이터 일관성
→ Saga / 보상 처리
→ Multi-instance
→ Load Balancer
→ Resilience
→ Distributed Observability
```

### Phase 8 — Modular Monolith와 서비스 경계

후보 도메인:

```text
Member
Article
Comment
Engagement
 ├─ Like
 └─ View
Ranking
Query
```

핵심은 서비스를 만드는 것이 아니라 데이터 소유권, 트랜잭션 경계, 의존 관계를 기준으로 서비스 경계를 찾는 방법을 이해하는 것이다.

### Phase 9 — Microservice 분리

Phase 8에서 확인한 경계 중 일부만 실제 독립 프로세스로 분리한다.

후보:

- Article Service
- Engagement Service
- Ranking Service
- Query Service

Database per Service를 적용하고 기존 단일 `@Transactional`이 더 이상 유지되지 않는 상황을 직접 만든다.

### Phase 10 — 서비스 간 동기 통신과 장애 전파

RestClient, WebClient, OpenFeign 등은 필요에 따라 비교한다.

핵심 실험:

- Connection Refused
- Slow Response
- Timeout
- Retry
- Retry Storm
- Connection Pool 고갈
- Circuit Breaker
- Bulkhead
- Fallback

### Phase 11 — 분산 데이터 일관성

- Event Publish 실패
- Consumer 장애
- Duplicate Event
- Out-of-order Event
- 부분 처리 실패
- Kafka 장애
- Eventual Consistency
- Transactional Outbox
- Idempotent Consumer
- Event Versioning
- Compensation
- Saga

Saga는 기술 이름을 배우기 위해 넣지 않고 실제 여러 서비스에 걸친 비즈니스 트랜잭션이 필요할 때 적용한다.

### Phase 12 — Multi-instance와 Load Balancing

- Local Cache 불일치
- Local State
- Session 문제
- Sticky Session
- Shared Session
- Stateless 구조
- Health Check
- Instance Failure
- Graceful Shutdown
- Scale-out
- 병목이 DB / Redis / Kafka 등 다른 계층으로 이동하는 현상

### Phase 13 — Distributed Observability와 장애 실험

후보 기술:

```text
Metrics → Prometheus
Logs    → Loki
Trace   → OpenTelemetry / Tempo
View    → Grafana
```

분산 요청을 Trace로 연결하고 서비스 Slow Response, Instance Down, Redis/DB/Kafka 장애, Kafka Lag, Network Failure, Read Model 지연 등을 Metrics / Logs / Trace로 함께 분석한다.

---

# 5. 현재 코드 베이스라인

2026-08-26 기준으로 기존 CRUD와 중복 구현을 제거하고 학습을 위한 최소 베이스라인으로 초기화했다.

현재 `src/main/java`에는 비즈니스 도메인을 미리 구현하지 않는다.

현재 최소 구성:

- Java 21
- Spring Boot 4.1.0
- Gradle 9.5.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Bean Validation
- MySQL 8.4
- Docker Compose
- Actuator / Micrometer
- Testcontainers
- JUnit 5 / AssertJ
- Lombok

현재 제거된 항목:

- 기존 Member / Article / Comment / Like CRUD
- Spring Security
- Flyway
- Redis
- Kafka
- CQRS / Read Model
- Distributed Lock
- 그 외 이후 Phase에서 필요해질 기술

Flyway는 현재 사용하지 않는다. 초기에는 구조를 빠르게 변경하며 실험하기 위해 JPA `ddl-auto=create`를 사용한다. 이후 Schema Evolution, 배포, 버전 관리 문제가 실제로 등장하면 Flyway 또는 다른 마이그레이션 도구의 도입을 다시 판단한다.

로컬 기본 포트:

```text
Spring Boot : 8081
MySQL       : 3310
```

현재 로컬 검증 결과:

- `./gradlew clean test` 성공
- Testcontainers 기반 테스트 성공
- Docker MySQL 연결 성공
- Spring Boot `bootRun` 성공
- Actuator 엔드포인트 노출 성공
- 현재 JPA Repository 0개는 의도한 초기 상태

Java 로컬 기준:

```text
Java 21.0.12
JAVA_HOME = C:\Program Files\Java\jdk-21.0.12
```

---

# 6. Git 브랜치 운영 규칙

브랜치는 **Phase 단위로 하나만 사용한다.**

Phase 내부의 Pagination, Index, Replication, Sharding 등을 각각 별도 브랜치로 나누지 않는다.

예시:

```text
main
  ↓
phase/0-system-foundation
  ↓ PR + merge
main
  ↓
phase/1-large-scale-article
  ↓ PR + merge
main
  ↓
phase/2-hierarchical-comment
  ↓ PR + merge
```

권장 브랜치 이름:

```text
phase/0-system-foundation
phase/1-large-scale-article
phase/2-hierarchical-comment
phase/3-like-concurrency
phase/4-high-write-traffic
phase/5-event-driven
phase/6-query-optimization
phase/7-integration
phase/8-modular-monolith
phase/9-microservice
phase/10-sync-communication
phase/11-distributed-consistency
phase/12-multi-instance
phase/13-distributed-observability
```

Phase가 끝나면 PR을 통해 `main`으로 병합하고 다음 Phase 브랜치를 `main`에서 새로 만든다.

동일 문제의 대체 전략을 비교하기 위해 임시 구현이 필요한 경우에는 해당 Phase 브랜치 안에서 커밋으로 비교하거나 필요하면 단기 실험 브랜치를 사용할 수 있지만, 기본 운영 규칙은 Phase당 하나의 브랜치다.

---

# 7. Issue 운영 규칙

Issue는 Spring 기본 구현 기록용이 아니다.

다음과 같은 내용은 보통 Issue를 만들지 않는다.

```text
Article Entity 생성
Controller 구현
DTO 분리
Repository 생성
Validation 적용
일반 CRUD 구현
```

Issue는 **실험, 측정, 개선, 비교, Trade-off를 기록할 가치가 있을 때만** 만든다.

예시:

```text
[Phase 1] OFFSET 깊이에 따른 페이지네이션 성능 비교
[Phase 1] 인덱스 적용 전후 실행 계획 비교
[Phase 1] Auto Increment / UUID / Snowflake ID 비교
[Phase 1] Primary-Replica 복제 지연 실험
[Phase 1] Hash Sharding / Range Sharding 비교
[Phase 1] Hot Shard 재현 및 분석

[Phase 3] Atomic Update / Optimistic / Pessimistic Lock 비교
[Phase 3] Multi-instance에서 Local Lock 한계 재현

[Phase 4] MySQL Hot Row와 Redis INCR 비교

[Phase 5] DB Commit 성공 / Kafka Publish 실패 재현
```

Issue 기본 구조:

```markdown
## 문제
왜 이 실험을 하는지

## 실험
데이터 규모, 부하 조건, 비교 대상

## 결과
TPS, p95/p99, 실행 계획, Lock Wait, Lag 등

## 해석
왜 이런 결과가 나왔는지

## Trade-off
각 방식의 장점, 비용, 적용 조건

## 결론
현재 실험에서 내린 판단
```

필요하지 않은 항목은 생략할 수 있다.

---

# 8. PR 운영 규칙

PR은 **Phase 전체의 종합 기록 + main 반영 단위**로 사용한다.

예시 제목:

```text
[Phase 1] 게시글과 대규모 데이터베이스 학습
[Phase 3] 좋아요와 동시성 학습
[Phase 5] 인기글과 이벤트 기반 아키텍처 학습
```

PR 기본 구조:

```markdown
## 학습 범위
이번 Phase에서 다룬 주요 범위

## 주요 실험
관련 Issue 링크와 핵심 실험

## 주요 결과
어떤 병목을 확인했고 어떤 개선이 효과가 있었는지

## 설계 판단과 Trade-off
채택한 방식과 선택하지 않은 방식

## 현재 한계
다음 Phase 또는 이후 확장에서 남은 문제
```

PR은 단순 변경 파일 목록보다 **Phase에서 시스템이 어떻게 변화했고 무엇을 배웠는지**를 요약하는 역할을 한다.

---

# 9. Wiki / README / Issue / PR의 역할

```text
README
→ 전체 공식 로드맵과 Phase별 세부 학습 범위

이 인수인계 문서
→ 프로젝트 목적, 방향, 운영 규칙, 현재 상태, 새 세션 인계

Issue
→ 실험 / 측정 / 개선 / 비교 / Trade-off 기록

PR
→ Phase 전체 종합 + main 병합 기록

Wiki
→ 장기적으로 다시 참고할 가치가 큰 개념, Phase 종합, 시스템 설계 기록
```

Wiki는 모든 사소한 학습 내용을 저장하는 장소가 아니다. Sharding, 동시성 전략 비교, Transactional Outbox, CQRS, Saga, 분산 장애 분석처럼 나중에 다시 참고할 가치가 큰 내용을 중심으로 정리한다.

---

# 10. 세션 인수인계 규칙

새 ChatGPT 세션에서 `large-scale-board`를 이어갈 때 다음 순서로 확인한다.

1. 이 문서를 읽는다.
2. `README.md`의 최신 전체 로드맵을 읽는다.
3. GitHub에서 현재 `main` HEAD를 다시 확인한다.
4. 현재 열려 있는 Phase 브랜치와 PR을 확인한다.
5. 현재 Phase의 관련 Issue를 확인한다.
6. 과거 대화만으로 local / PR / CI 상태를 추측하지 않는다.
7. 현재 Phase에서 어디까지 구현·실험했는지 GitHub 상태를 기준으로 이어간다.

동적 상태는 항상 다시 확인한다.

예:

- main HEAD
- Phase branch HEAD
- PR 상태
- CI 상태
- Issue 상태
- 실제 merged 여부

과거 대화의 상태를 최신 상태로 간주하지 않는다.

---

# 11. ChatGPT의 역할

이 프로젝트에서 ChatGPT는 단순 코드 생성기가 아니라 **학습 설계와 실험 파트너** 역할을 한다.

주요 역할:

- 현재 Phase의 학습 범위 설명
- 문제 상황과 재현 방법 설계
- 필요한 개념과 내부 동작 설명
- 대안 및 Trade-off 비교
- 구현 범위 제안
- 실험 조건과 측정 지표 설계
- 코드 리뷰
- SQL / 실행 계획 / 로그 / 메트릭 해석
- 장애 실험 설계
- 결과 해석
- Issue / PR / Wiki 기록 정리
- 다음 Phase로 넘어갈 조건 판단

사용자가 직접 이해하고 구현하는 과정이 핵심이므로 중요한 설계 판단을 자동으로 건너뛰지 않는다.

Codex는 현재 이 프로젝트의 기본 진행 도구로 사용하지 않는다.

---

# 12. 다음 진행 기준

현재는 **새 베이스라인을 만든 직후이며 Phase 0부터 다시 시작하는 상태**다.

현재 최소 구조:

```text
Client
  ↓
Spring Boot :8081
  ↓
HikariCP / JPA
  ↓
MySQL :3310
```

다음 학습은 README의 Phase 0을 기준으로 진행한다.

Phase 0을 단순 환경 구축 체크리스트로 끝내지 않고 다음을 현재 실행 중인 시스템과 연결해 이해한다.

- Spring Boot 프로세스와 JVM
- Tomcat 요청 처리
- HikariCP와 DB Connection
- Docker Container / Network / Volume
- Actuator / Micrometer
- JVM / HTTP / HikariCP 기본 지표
- Scale-up / Scale-out
- Stateful / Stateless
- Load Balancer
- Monolith / MSA

Phase 0의 기준선을 이해하고 측정 가능한 환경을 만든 뒤 Phase 1에서 최초의 실제 게시글 도메인을 추가한다.

---

# 13. 최종 지향점

초기 시스템:

```text
Client
→ Spring Boot
→ MySQL
```

최종적으로는 필요에 의해 다음과 같은 구조까지 발전시키는 것을 목표로 한다.

```text
Client
   ↓
Load Balancer / Gateway
   ↓
여러 Application / Service Instance
   ↓
Service-to-Service Communication
   ↓
각 서비스 DB

      ↕
    Redis

      ↕
    Kafka
      ↓
Transactional Outbox
      ↓
Consumer
      ↓
CQRS Read Model

Metrics / Logs / Trace
      ↓
Prometheus / Loki / Tempo / Grafana
```

중요한 것은 이 최종 그림을 빠르게 만드는 것이 아니다.

각 구조가 없었을 때 발생하는 문제를 먼저 경험하고, 그 문제를 해결하는 과정에서 시스템이 자연스럽게 이 구조에 가까워지는 것이 프로젝트의 최종 목표다.
