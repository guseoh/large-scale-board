# large-scale-board

단순한 게시판 시스템에서 시작해 데이터와 트래픽을 증가시키고, 그 과정에서 발생하는 병목·동시성·데이터 정합성·장애 문제를 직접 재현한 뒤 시스템을 점진적으로 발전시키는 대규모 시스템 및 분산 시스템 설계 학습 프로젝트다.

완성된 서비스나 많은 기술 사용 자체가 목표가 아니다. 각 기술이 없을 때 어떤 문제가 생기는지 먼저 경험하고, 여러 해결 후보를 비교한 뒤 구현과 측정으로 선택을 검증하는 것이 핵심이다.

## 학습 원칙

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

- 성능 개선 전에는 반드시 현재 상태를 측정한다.
- 기술을 로드맵에 적혀 있다는 이유만으로 미리 도입하지 않는다.
- 하나의 문제에 여러 해결 방법을 적용하고 결과를 비교할 수 있다.
- 정상 상황뿐 아니라 과부하, 장애, 데이터 유실·중복·지연을 적극적으로 실험한다.
- 모놀리스에서 경험한 문제를 이후 분산 시스템에서 다시 경험하고 차이를 비교한다.
- 학습 프로젝트이므로 데이터베이스, 캐시, 메시징, 관측, 인프라 등 다양한 기술을 폭넓게 실험한다.
- 기술을 사용했다는 사실보다 왜 필요했고 어떤 비용이 추가됐는지를 설명할 수 있어야 한다.

## 참고 자료 사용 방식

[인프런 - 스프링부트로 직접 만들면서 배우는 대규모 시스템 설계 - 게시판](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8%EB%A1%9C-%EB%8C%80%EA%B7%9C%EB%AA%A8-%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%84%A4%EA%B3%84-%EA%B2%8C%EC%8B%9C%ED%8C%90?cid=334365)의 공개 목차는 학습 주제와 기술 범위를 참고하는 자료로 사용한다. 강의를 구매해 순서대로 따라가는 프로젝트가 아니며, 실제 진행 순서는 이 README의 로드맵과 프로젝트에서 재현된 문제를 기준으로 한다.

## 현재 베이스라인

현재 코드는 Phase 0을 시작하기 위한 최소 실행 환경만 유지한다.

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- MySQL 8.4
- Docker Compose
- Testcontainers
- Actuator / Micrometer
- Validation
- Lombok
- JUnit 5 / AssertJ

초기 베이스라인에는 비즈니스 도메인, Spring Security, Flyway, Redis, Kafka, CQRS, 분산 락 등 이후 학습 기술을 미리 구현하지 않는다.

Flyway 역시 현재는 사용하지 않는다. 초기에는 JPA 스키마 생성을 이용해 빠르게 구조를 변경하며 실험하고, 스키마 버전 관리와 배포 문제가 실제 학습 주제로 등장하면 Flyway 또는 다른 마이그레이션 도구의 재도입을 검토한다.

---

# Part 1. 대규모 단일 시스템

하나의 Spring Boot 애플리케이션과 데이터베이스에서 출발해 대규모 데이터, 동시성, 캐시, 이벤트 처리와 조회 최적화를 경험한다.

```text
단순 Spring Boot 애플리케이션
→ 대규모 데이터
→ DB 병목
→ 인덱스와 조회 최적화
→ Replication / Sharding
→ 동시성
→ Redis
→ Kafka
→ Transactional Outbox
→ CQRS / Read Model
→ Cache
→ 장애 및 성능 실험
```

## Phase 0 — 시스템 기반

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

Phase 0에서는 다음 Phase의 기술을 미리 구현하지 않는다. 실행 환경과 측정 가능한 최소 애플리케이션을 만든 뒤 단순한 도메인부터 추가한다.

## Phase 1 — 대규모 게시글과 DB

```text
단일 DB
→ 대량 데이터
→ 병목 측정
→ Index / Query 개선
→ 단일 DB 한계 확인
→ Replication
→ Sharding
```

학습 범위:

- 게시글 기본 모델과 API
- 10만 / 100만 건 이상의 데이터
- Auto Increment / UUID / Snowflake ID
- OFFSET / Cursor Pagination
- COUNT 비용
- Clustered / Secondary / Covering Index
- 복합 인덱스 컬럼 순서
- `EXPLAIN ANALYZE`
- Slow Query
- Replication / Read Scaling
- Sharding
- Shard Key
- Hash / Range Sharding
- Hot Shard
- Cross-Shard Query
- 일부 Shard 장애

## Phase 2 — 계층형 댓글

- Adjacency List
- Path Enumeration
- Materialized Path
- Closure Table
- 계층 조회와 정렬
- 삭제 전략
- 깊은 트리 / 넓은 트리
- 읽기와 쓰기 비용 비교

하나의 모델을 정답으로 두지 않고 같은 댓글 요구사항을 여러 모델로 구현해 비용을 비교한다.

## Phase 3 — 좋아요와 동시성

```text
중복 요청
→ DB Constraint

Lost Update
→ Atomic Update

동시 충돌
→ Optimistic / Pessimistic Lock

Multi-instance
→ Local Lock 한계
→ Redis Distributed Lock
```

비교 대상:

- DB Constraint
- Atomic Update
- Optimistic Lock
- Pessimistic Lock
- 멱등성
- 재시도
- Local Lock
- Redis Distributed Lock
- Redis Lua 기반 원자 처리

처리량, 충돌 수, 재시도, p95/p99, DB Lock Wait 등을 같은 조건에서 비교한다.

## Phase 4 — 조회수와 높은 쓰기 트래픽

```text
MySQL 직접 UPDATE
→ Hot Row
→ Lock 경합
→ Redis INCR
→ Write-back
→ Batch Flush
```

성능뿐 아니라 다음도 확인한다.

- DB 반영 지연
- Redis 장애
- Flush 실패
- 애플리케이션 종료 시 데이터 유실 범위
- 복구 전략

## Phase 5 — Kafka와 Event Driven Architecture

Kafka를 단순 Producer / Consumer 예제로 끝내지 않는다.

- Topic / Partition / Offset
- Consumer Group
- Ordering
- At-least-once
- Duplicate Event
- Idempotent Consumer
- Retry / Backoff
- DLQ
- Consumer Lag
- Producer / Consumer 장애
- 이벤트 적체와 복구

특히 다음 문제를 재현한다.

```text
DB Commit 성공
Kafka Publish 실패
```

그 뒤 Transactional Outbox, Outbox Relay, 재시도와 장애 복구를 학습한다.

## Phase 6 — CQRS와 조회 최적화

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

CQRS는 CommandService와 QueryService 클래스 분리에 그치지 않고 Write Model과 Read Model을 실제 데이터 구조 수준에서 분리한다.

학습 범위:

- Read Model
- Eventual Consistency
- Cache Aside
- Cache Invalidation
- Cache Stampede
- Hot Key
- Request Collapsing
- Redis 장애 시 DB Fallback

## Phase 7 — 전체 시스템 통합

Part 1에서 만든 시스템을 대상으로 다음 장애를 다시 검증한다.

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

이 단계까지를 대규모 단일 시스템 학습으로 본다.

---

# Part 2. 분산 시스템

Part 1을 모두 경험한 뒤 기존 모놀리스를 실제 분산 시스템으로 발전시킨다. 처음부터 MSA를 만들지 않는다.

```text
기존 시스템의 도메인 경계 분석
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

## Phase 8 — Modular Monolith와 서비스 경계

기존 코드에서 Member, Article, Comment, Engagement, Ranking, Query 등의 실제 경계를 찾는다.

확인 대상:

- 도메인 간 의존 관계
- Transaction Boundary
- DB FK
- 강한 결합
- 순환 의존성
- 다른 도메인 내부 모델 직접 참조
- 이벤트로 바꿀 수 있는 경계

목표는 서비스를 많이 만드는 것이 아니라 서비스 경계를 찾는 방법을 이해하는 것이다.

## Phase 9 — Microservice 분리

Phase 8에서 확인한 경계 중 일부만 실제 프로세스로 분리한다.

후보:

- Article Service
- Engagement Service
- Ranking Service
- Query Service

각 서비스가 자신의 데이터를 소유하도록 하여 Database per Service를 경험하고, 기존 단일 애플리케이션 트랜잭션이 사라지는 문제를 만든다.

## Phase 10 — 서비스 간 동기 통신과 장애 전파

RestClient, WebClient, OpenFeign 등은 필요에 따라 비교한다. 핵심은 라이브러리 사용법이 아니라 장애 전파다.

실험:

- Connection Refused
- Slow Response
- Timeout
- Retry
- Retry Storm
- Connection Pool 고갈

필요성을 확인한 뒤 다음을 적용하고 비교한다.

- Timeout
- Retry / Backoff
- Circuit Breaker
- Bulkhead
- Fallback

## Phase 11 — 분산 데이터 일관성

서비스 분리 후 단일 `@Transactional`이 사라진 상황을 실험한다.

- Event Publish 실패
- Consumer 장애
- Duplicate Event
- Out-of-order Event
- 부분 처리 실패
- Producer 재시작
- Kafka 장애

학습 대상:

- Eventual Consistency
- Transactional Outbox
- Idempotent Consumer
- Retry
- DLQ
- Event Versioning
- Compensation
- Saga

Saga는 이름을 배우기 위해 넣지 않고 여러 서비스에 걸친 실제 비즈니스 트랜잭션이 필요할 때 적용한다.

## Phase 12 — Multi-instance와 Load Balancing

서비스를 여러 인스턴스로 실행한다.

실험 대상:

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

Scale-out 전후 TPS뿐 아니라 DB Connection, Redis Connection, Kafka Consumer Group 등 병목이 다른 계층으로 이동하는 현상도 확인한다.

## Phase 13 — Distributed Observability와 장애 실험

후보 기술:

```text
Metrics → Prometheus
Logs    → Loki
Trace   → OpenTelemetry / Tempo
View    → Grafana
```

하나의 요청을 여러 서비스와 저장소를 거쳐 Trace로 연결하고 다음 장애를 직접 만든다.

- 특정 서비스 Slow Response
- 특정 Instance Down
- Redis Down
- DB Down
- Kafka Consumer Down
- Kafka Lag 증가
- Network Failure
- Read Model 지연

Metrics, Logs, Trace를 함께 사용해 원인을 좁혀가는 과정을 학습한다.

---

# 최종 목표

초기에는 다음과 같은 단순한 구조에서 시작한다.

```text
Client
  ↓
Spring Boot
  ↓
MySQL
```

학습을 거치면서 필요에 의해 다음과 같은 구조까지 발전시킨다.

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

최종 그림을 미리 만드는 것이 목적은 아니다. 각 구조가 없었을 때 발생하는 문제를 먼저 경험하고, 해결하면서 최종 구조까지 발전시키는 과정 자체가 프로젝트의 목표다.
