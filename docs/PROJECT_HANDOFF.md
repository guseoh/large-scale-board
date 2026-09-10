# large-scale-board 프로젝트 인수인계 및 학습 운영 기준

> 기준일: 2026-09-10
>
> 저장소: `guseoh/large-scale-board`
>
> 이 문서는 새 ChatGPT 세션이나 새로운 학습 세션이 기존 대화 맥락 없이도 프로젝트를 즉시 이어갈 수 있도록 만든 **프로젝트 인수인계 기준 문서**다.
>
> 전체 Phase별 세부 학습 범위와 완료 조건은 `README.md`가 최우선 기준이다. 이 문서는 프로젝트의 방향성, 학습 철학, 설계 원칙, ChatGPT 진행 방식, Git/Issue/PR 운영 규칙, 현재 베이스라인과 다음 진행 지점을 정리한다.

---

# 1. 프로젝트의 본질

`large-scale-board`는 완성된 게시판 서비스를 빠르게 만드는 프로젝트가 아니다.

또한 Redis, Kafka, Sharding, MSA 같은 기술을 많이 사용해 보는 것이 목적도 아니며, 단순히 대규모 트래픽을 체험하는 프로젝트도 아니다.

이 프로젝트의 핵심 목적은 **AI가 구현을 쉽게 대신할 수 있는 시대에도 백엔드 개발자가 시스템의 동작 원리와 문제 원인을 스스로 이해하고, 설계와 기술 선택을 판단할 수 있는 근본적인 역량을 기르는 것**이다.

이를 위해 비교적 단순한 Spring Boot + MySQL 게시판에서 출발해 시스템을 직접 설계하고 구현한다. 이후 데이터와 트래픽을 늘리면서 병목, 동시성, 데이터 정합성, 장애, 운영 문제를 의도적으로 만들고 여러 해결 전략을 비교한다.

학습 대상은 특정 기술에 한정하지 않는다.

```text
Java / JVM
Spring / Spring MVC / Spring Data JPA
JPA / Hibernate
Database / SQL / Transaction / Lock / Index
OS / Thread / Process / Memory / I/O
Network / TCP / HTTP / Connection
Concurrency
Data Structure
Cache
Messaging
Infrastructure
Observability
Distributed System
```

이 지식들을 별개의 암기 과목으로 다루기보다 **하나의 실제 시스템이 동작하고 느려지고 실패하고 확장되는 흐름 속에서 서로 연결해 이해하는 것**을 목표로 한다.

최종적으로는 다음 질문에 코드, 설계와 측정 결과를 근거로 답할 수 있어야 한다.

```text
이 시스템은 지금 어떻게 동작하는가?
왜 이 구조로 설계했는가?
현재 문제는 어느 계층에서 발생하는가?
왜 이 문제가 발생하는가?
어떤 해결 후보가 있는가?
각 후보의 장점과 비용은 무엇인가?
어떤 조건에서 하나를 선택해야 하는가?
실제 측정 결과가 설계 가정과 일치하는가?
장애가 발생하면 어디까지 전파되는가?
데이터의 유실·중복·지연·불일치 범위를 설명할 수 있는가?
AI가 제안한 구현이나 기술 선택이 이 시스템에 적합한지 검토할 수 있는가?
```

최종 아키텍처 자체보다 **단순한 구조가 왜 점점 복잡한 구조로 발전했는지 설명할 수 있는 과정**을 더 중요하게 본다.

---

# 2. 학습 깊이 기준

이 프로젝트는 구현만 빠르게 진행하지 않는다. 코드에 중요한 개념이 등장하면 왜 사용하는지와 현재 시스템에서 어떤 역할을 하는지 학습한 뒤 진행한다.

예를 들어 Spring Data JPA Query Method가 처음 등장한다면 단순히 메서드 코드만 추가하지 않는다.

다음과 같은 흐름을 현재 구현에 필요한 범위까지 이해한다.

```text
Repository가 왜 필요한가
→ Spring Data JPA가 무엇을 제공하는가
→ 구현 클래스 없이 Repository가 동작하는 이유
→ Query Method가 메서드 이름을 어떻게 해석하는가
→ JPA / Hibernate / SQL과 어떻게 연결되는가
→ 실제 실행되는 Query 확인
→ Query Method가 편리한 범위와 한계
```

하지만 모든 개념을 동일한 깊이로 파고들지는 않는다.

핵심 기준은 다음 두 가지다.

1. **현재 시스템과 코드 흐름을 이해하는 데 필요한가?**
2. **백엔드 개발자가 설계·운영·트러블슈팅에서 판단하기 위해 알아야 하는가?**

두 기준에 해당하는 내용은 충분히 깊게 학습한다.

반대로 현재 문제와 직접 관련 없는 프레임워크 내부 구현 세부사항이나 학술적 확장은 필요할 때 별도 심화 학습으로 분리할 수 있다.

예를 들어 `@Transactional`을 처음 사용할 때는 트랜잭션 경계와 Spring의 기본 동작을 이해하고 넘어간다. 이후 Phase 3에서 Lost Update가 실제 문제로 등장하면 Transaction, Persistence Context, Flush, MySQL Isolation Level, MVCC, Record Lock까지 다시 깊게 연결해 학습한다.

따라서 이 프로젝트의 원칙은 다음과 같다.

> 모든 것을 끝까지 깊게 파지는 않지만, 현재 문제를 설명하는 핵심 원리와 백엔드 개발자가 판단에 필요한 내용은 대충 넘어가지 않는다.

더 깊게 알고 싶은 세부 개념은 별도의 개념 학습 세션이나 자료 조사로 확장할 수 있다.

---

# 3. CS 지식 학습 방식

CS 지식을 별도 체크리스트로 억지로 삽입하지 않는다.

실제 시스템 문제에서 필요해지는 순간 연결해서 학습한다.

예를 들어 TPS가 더 이상 증가하지 않을 때 단순히 HikariCP 설정부터 바꾸지 않는다.

```text
HTTP Request
→ Tomcat Worker Thread
→ Application Code
→ HikariCP Connection 획득
→ JDBC / Socket
→ MySQL Connection
→ Query
→ Buffer Pool / Storage
```

이 흐름을 따라가면서 문제에 필요한 Thread, Blocking I/O, Socket, TCP Connection, Queue, Context Switching, Memory, Buffer 같은 개념을 학습한다.

Redis, Kafka, Load Balancer와 같은 기술도 마찬가지다. 사용법보다 먼저 해당 기술이 해결하려는 문제와 기반 원리를 이해한다.

---

# 4. 프로젝트 진행의 기본 루프

모든 주요 학습 단위는 가능한 한 다음 흐름을 따른다.

```text
현재 Phase와 시스템 상태 확인
→ 요구사항과 설계
→ 현재 구현에 필요한 개념 학습
→ 단순 구현
→ 동작 흐름 확인
→ 문제 재현
→ 측정
→ 원인 분석
→ 해결 후보의 원리 학습
→ 대안과 Trade-off 비교
→ 설계 결정
→ 개선 구현
→ 동일 조건 재측정
→ 장애 실험
→ 결과 해석
→ 설계 수정
```

추가 원칙:

- 기술을 로드맵에 적혀 있다는 이유만으로 미리 도입하지 않는다.
- 성능 개선 전에 현재 상태와 병목을 먼저 측정한다.
- 가능한 경우 동일 문제에 여러 해결 전략을 적용해 비교한다.
- 정상 상황만 확인하지 않고 과부하, 장애, 데이터 유실, 중복, 지연과 복구를 적극적으로 만든다.
- 평균 응답 시간만 보지 않고 문제에 맞는 TPS, p50, p95, p99, 오류율, Lock Wait, Connection Pool Wait, Consumer Lag 등을 함께 본다.
- Before / After는 가능한 한 같은 데이터와 같은 부하 조건에서 비교한다.
- 하나의 실험에서는 가능한 한 하나의 주요 변수를 변경한다.
- 실패한 실험과 예상과 다른 결과도 학습 결과다.
- 포트폴리오 프로젝트가 아니므로 학습 가치가 있다면 여러 기술과 접근을 폭넓게 실험할 수 있다.
- 하지만 모든 기술은 `왜 필요한가`, `어떻게 동작하는가`, `대안은 무엇인가`, `무슨 비용과 새로운 실패 지점이 생기는가`를 설명할 수 있어야 한다.

---

# 5. Project Design Baseline — 구현 전 프로젝트 설계

현재 가장 먼저 진행할 학습 단위다.

DB부터 만들거나 Article Entity부터 작성하지 않는다. 먼저 **대규모 게시판이라는 단순한 도메인을 충분히 이해하고 이후 시스템 발전의 기준선으로 사용할 수 있을 정도로 구체적인 프로젝트 설계**를 만든다.

거대한 PRD나 완성형 분산 아키텍처를 미리 설계하는 것이 목적은 아니다.

초기 설계는 현재 시스템의 목적, 핵심 데이터와 요청 흐름을 이해하기 위한 기준선이며 이후 Phase의 문제와 실험을 통해 계속 변화한다.

초기 설계 순서:

```text
1. 프로젝트 목적과 범위
2. Actor와 핵심 Use Case
3. 핵심 Business Rule
4. 전체 Domain Map
5. 초기 데이터 모델과 ERD
6. 초기 API와 요청 흐름
7. 초기 Application / Database Architecture
8. 비기능 요구사항 후보
9. 이후 검증할 부하·병목·장애 가설
10. 현재 Phase에서 실제 구현할 Physical Design 결정
```

초기 전체 도메인은 개념 수준에서 다음 후보를 볼 수 있다.

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

하지만 전체 도메인을 처음부터 코드나 테이블로 구현하지 않는다.

## Conceptual Design과 Physical Design을 구분한다

**Conceptual Design**에서는 앞으로 존재할 수 있는 도메인과 관계를 전체적으로 이해한다.

예를 들어 Article에 Comment, Like, View가 연결될 것이라는 사실은 초기 설계에서 알고 있어도 된다.

하지만 **Physical Design과 실제 구현은 현재 Phase에 필요한 범위만 만든다.**

예:

```text
초기 설계
→ Member / Article / Comment / Like / View의 관계 인지

Phase 1
→ 현재 게시글 학습에 필요한 물리 구조만 구현

Phase 2
→ 계층형 댓글 문제를 경험하고 Comment 모델 재설계

Phase 3
→ Like 중복·동시성 문제와 함께 Constraint와 Lock 전략 설계

Phase 4
→ View 높은 쓰기 트래픽을 경험하고 저장 위치와 일관성 요구 재설계
```

## ERD와 아키텍처는 진화하는 학습 산출물이다

초기 ERD 하나를 정답으로 확정하지 않는다.

```text
초기 ERD
→ 계층형 댓글 설계
→ Engagement와 동시성 제약 추가
→ Outbox / Event 모델 추가
→ CQRS Read Model 분리
→ 서비스 분리
→ Database per Service
```

각 변화에서 다음 질문을 남긴다.

```text
기존 설계는 무엇이었는가?
어떤 요구사항이나 문제가 기존 설계를 부족하게 만들었는가?
대안은 무엇이었는가?
왜 현재 설계를 선택했는가?
무슨 복잡성과 실패 가능성이 새로 생겼는가?
```

이것이 이 프로젝트에서 시스템 설계를 학습하는 핵심 방식이다.

---

# 6. 참고 강의와 외부 자료 사용 방식

참고 자료:

- 인프런 공개 강의 페이지: `스프링부트로 직접 만들면서 배우는 대규모 시스템 설계 - 게시판`

이 강의는 구매하지 않았으며 **공개 목차에서 다루는 주제와 기술 범위만 참고한다.**

사용자가 원하는 것은 강의 구현을 복제하는 것이 아니라, 해당 강의가 지향하는 것처럼 **대규모 데이터와 트래픽을 고려할 때 어떤 문제를 발견하고 무엇을 고민하며 어떤 근거로 해결책을 선택하는지 학습하는 과정**이다.

```text
README 로드맵
= 프로젝트의 실제 학습 순서와 공식 범위

인프런 공개 목차
= 관련 시스템 설계 / MySQL / Redis / Kafka / 동시성 주제 참고
```

필요한 경우 공식 문서, 기술 서적, 국내외 IT 기업 기술 블로그, 검증된 기술 블로그, 오픈소스 프로젝트와 장애 사례도 적극적으로 참고한다.

---

# 7. 전체 로드맵

전체 세부 항목과 완료 조건은 반드시 `README.md`를 기준으로 한다.

## Part 1 — 대규모 단일 시스템

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
- Spring Boot / JVM / Tomcat
- MySQL
- JPA / Hibernate
- HikariCP
- Testcontainers
- Actuator / Micrometer
- JVM / HTTP / Connection Pool 기본 관측

### Phase 1 — 게시글과 대규모 데이터베이스

```text
단일 DB
→ 대량 데이터
→ Query / Index 병목
→ Pagination
→ ID 전략
→ Replication
→ Sharding
```

핵심 범위:

- Auto Increment / UUID / Snowflake
- 10만 / 100만 데이터
- OFFSET / Cursor
- Clustered / Secondary / Covering Index
- Composite Index
- `EXPLAIN ANALYZE`
- `COUNT(*)`
- Primary / Replica
- Replication Lag / Read-after-write
- Shard Key
- Hash / Range Sharding
- Hot Shard
- Cross-Shard Query
- Partial Shard Failure

### Phase 2 — 계층형 댓글

- Adjacency List
- Path Enumeration
- Materialized Path
- Closure Table
- 트리 조회 / 정렬 / 삭제
- Logical / Physical Delete
- 모델별 Read / Write 비용

### Phase 3 — 좋아요와 동시성

```text
Atomic Update
vs
Optimistic Lock
vs
Pessimistic Lock
vs
Redis Distributed Lock
```

- Unique Constraint
- Lost Update
- Isolation Level
- Record Lock
- Retry / Idempotency
- Multi-thread / Multi-instance
- Redis Lua
- TPS / p95 / p99 / Lock Wait / Retry 비교

### Phase 4 — 조회수와 높은 쓰기 트래픽

```text
MySQL UPDATE
→ Hot Row
→ Lock 경합
→ Redis INCR
→ Write-back
→ Batch Flush
```

Redis 장애, 동기화 지연과 데이터 유실 범위까지 실험한다.

### Phase 5 — 인기글과 Event-Driven Architecture

- Kafka Topic / Partition / Offset / Consumer Group
- Ordering / At-least-once
- Duplicate Event / Idempotent Consumer
- Retry / Backoff / DLQ
- DB Commit 성공 + Kafka Publish 실패
- Transactional Outbox
- Outbox Relay
- CDC / Transaction Log Tailing
- Consumer Lag / 적체 / 복구

### Phase 6 — 게시글 조회 최적화

```text
복잡한 Query
→ CQRS Read Model
→ Eventual Consistency
→ Cache Aside
→ Cache Stampede
→ Hot Key
→ Request Collapsing
```

CQRS는 CommandService / QueryService 클래스 분리가 아니라 실제 Write Model과 Read Model의 데이터 구조 분리를 의미한다.

### Phase 7 — 전체 시스템 통합과 검증

MySQL, Redis, Kafka, Consumer, Outbox Relay, Cache, Read Model, Shard 일부 장애를 통합해서 검증한다.

## Part 2 — 분산 시스템

```text
도메인 경계 분석
→ Modular Monolith
→ 일부 서비스 실제 분리
→ Database per Service
→ 서비스 간 동기 통신
→ 네트워크 실패와 장애 전파
→ 이벤트 통신
→ 분산 데이터 일관성
→ Compensation / Saga
→ Multi-instance
→ Load Balancer
→ Distributed Observability
```

### Phase 8 — Modular Monolith와 서비스 경계

데이터 소유권, 변경 이유, 트랜잭션 경계와 의존 관계를 기준으로 서비스 후보를 찾는다.

### Phase 9 — Microservice 분리

필요성이 확인된 일부 경계만 독립 프로세스와 Database per Service로 실제 분리한다.

### Phase 10 — 서비스 간 동기 통신과 장애 전파

- Slow Response
- Timeout
- Retry / Backoff / Jitter
- Retry Storm
- Connection Pool Exhaustion
- Circuit Breaker
- Bulkhead
- Fallback

### Phase 11 — 분산 데이터 일관성

- Eventual Consistency
- Duplicate / Out-of-order Event
- Transactional Outbox
- Idempotent Consumer
- Event Versioning
- Compensation
- Saga

Saga는 실제 보상 트랜잭션 필요성이 확인될 때만 도입한다.

### Phase 12 — Multi-instance와 Load Balancing

- Local State / Local Cache
- Session
- Sticky / Shared / Stateless
- Health Check
- Graceful Shutdown
- Scale-out
- 병목 이동
- Kafka Consumer Rebalance

### Phase 13 — Distributed Observability와 장애 실험

```text
Metrics → Prometheus
Logs    → Loki
Trace   → OpenTelemetry / Tempo
View    → Grafana
```

분산 요청의 End-to-End 흐름과 장애 전파를 Metrics / Logs / Trace로 함께 분석한다.

---

# 8. 현재 코드 베이스라인

현재 `main`은 기존 비즈니스 구현을 제거하고 학습을 위한 최소 실행 환경으로 초기화한 상태다.

현재 구성:

- Java 21
- Spring Boot 4.1.0
- Gradle 9.5.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL 8.4
- Docker Compose
- Actuator / Micrometer
- Testcontainers
- JUnit 5 / AssertJ
- Lombok

현재 미리 구현하지 않는 항목:

- Member / Article / Comment / Like 등의 비즈니스 도메인
- Spring Security
- Flyway
- Redis
- Kafka
- CQRS / Read Model
- Distributed Lock
- 이후 Phase에서 필요해질 기술

Flyway는 현재 사용하지 않는다. 초기에는 구조를 빠르게 바꾸며 실험하기 위해 JPA `ddl-auto=create`를 사용하고, Schema Evolution과 배포 문제가 실제 학습 주제로 등장하면 다시 검토한다.

로컬 기본 포트:

```text
Spring Boot : 8081
MySQL       : 3310
```

2026-08-26 로컬 검증 완료:

- `docker compose` MySQL 기동 성공
- `docker compose ps`에서 MySQL 기동 확인
- Java 21.0.12 확인
- `JAVA_HOME = C:\Program Files\Java\jdk-21.0.12`
- `./gradlew.bat clean test` 성공
- Testcontainers 기반 테스트 성공
- Spring Boot `bootRun` 성공
- HikariCP → MySQL 연결 성공
- Hibernate / JPA 초기화 성공
- Actuator 3개 Endpoint 노출 로그 확인
- Tomcat 8081 기동 성공
- 현재 JPA Repository 0개는 의도된 초기 상태

현재는 **실행 환경 기준선이 확보됐지만 Project Design Baseline은 아직 작성하지 않은 상태**다.

---

# 9. Git 브랜치 운영 규칙

기본적으로 **Phase당 하나의 브랜치만 사용한다.**

Phase 내부의 Pagination, Index, Replication, Sharding 등을 각각 별도 브랜치로 나누지 않는다.

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

권장 이름:

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

Phase가 끝나면 PR을 통해 `main`에 병합하고 다음 Phase 브랜치를 최신 `main`에서 생성한다.

대체 전략을 비교하는 짧은 실험이 꼭 필요한 경우 단기 브랜치를 사용할 수 있지만 기본 원칙은 Phase당 하나다.

---

# 10. Issue 운영 규칙

Issue는 Spring 기본 구현이나 일반 CRUD 학습 기록용이 아니다.

보통 Issue를 만들지 않는 예:

```text
Article Entity 생성
Controller 구현
DTO 분리
Repository 생성
Query Method 추가
Validation 적용
일반 CRUD 구현
```

이러한 내용은 채팅에서 충분히 개념을 학습할 수 있지만 GitHub Issue에는 남기지 않는다.

Issue는 **실험, 측정, 개선, 대안 비교와 Trade-off를 기록할 가치가 있을 때만** 만든다.

예:

```text
[Phase 1] OFFSET 깊이에 따른 페이지네이션 성능 비교
[Phase 1] 인덱스 적용 전후 실행 계획 비교
[Phase 1] Auto Increment / UUID / Snowflake 비교
[Phase 1] Primary-Replica 복제 지연 실험
[Phase 1] Hash / Range Sharding 비교
[Phase 1] Hot Shard 재현

[Phase 3] Atomic / Optimistic / Pessimistic Lock 비교
[Phase 3] Multi-instance에서 Local Lock 한계

[Phase 4] MySQL Hot Row와 Redis INCR 비교

[Phase 5] DB Commit 성공 / Kafka Publish 실패
```

Issue 기본 구조:

```markdown
## 문제
왜 이 실험이 필요한가

## 실험
데이터 규모, 부하 조건, 비교 대상

## 결과
TPS, p95/p99, 실행 계획, Lock Wait, Lag 등

## 해석
왜 이런 결과가 나왔는가

## Trade-off
각 방식의 장점, 비용, 적용 조건

## 결론
현재 실험에서 내린 판단
```

---

# 11. PR 운영 규칙

PR은 **Phase 전체 종합 기록 + main 반영 단위**다.

```text
[Phase 1] 게시글과 대규모 데이터베이스 학습
[Phase 3] 좋아요와 동시성 학습
[Phase 5] 인기글과 이벤트 기반 아키텍처 학습
```

PR 기본 구조:

```markdown
## 학습 범위

## 주요 설계 변화

## 주요 실험

## 주요 결과

## 설계 판단과 Trade-off

## 현재 한계
```

PR은 단순 변경 파일 목록보다 **해당 Phase에서 시스템이 어떻게 변했고 무엇을 이해하고 검증했는지**를 남기는 역할을 한다.

---

# 12. README / Wiki / Issue / PR 역할

```text
README
→ 공식 전체 로드맵, Phase별 학습 범위와 완료 조건

Wiki Home / PROJECT_HANDOFF
→ 프로젝트 목적, 학습 철학, 진행 방식, 설계 원칙, 현재 상태, 새 세션 인수인계

Issue
→ 실험 / 측정 / 개선 / 대안 비교 / Trade-off 기록

PR
→ Phase 전체 설계·구현·실험 종합 + main 병합 기록

Wiki 추가 문서
→ 다시 참고할 가치가 큰 시스템 설계, Phase 종합, Sharding / Outbox / CQRS / Saga / 장애 분석 등의 장기 기록
```

Wiki는 모든 사소한 Spring 개념을 기록하는 노트가 아니다. 채팅에서 개념은 충분히 학습하되, Wiki에는 장기적으로 시스템을 이해하는 데 가치가 있는 설계와 결과를 중심으로 남긴다.

---

# 13. ChatGPT 프로젝트 진행 규칙

이 프로젝트는 ChatGPT의 `Large-Board` 프로젝트 안에서 진행하는 것을 기본으로 한다.

ChatGPT는 단순 코드 생성기가 아니라 **백엔드 튜터 + 시스템 설계 리뷰어 + 실험 파트너** 역할을 한다.

주요 역할:

- 현재 README / Wiki / GitHub 상태 확인
- 현재 Phase의 목표와 시스템 상태 설명
- 구현 전 요구사항과 설계 진행
- Domain / ERD / API / Request Flow / Architecture 설계 지원
- 구현에 등장하는 Java / Spring / JPA / DB / CS / Infra 개념 설명
- 사용자가 직접 구현할 범위 제시
- 코드 리뷰
- SQL / 실행 계획 / 로그 / 메트릭 해석
- 병목과 장애 원인 후보를 함께 좁히기
- 대안과 Trade-off 비교
- 실험 조건과 측정 지표 설계
- 장애 실험 설계
- 결과와 설계 가정 비교
- Issue / PR / Wiki 정리
- 다음 Phase 진입 조건 판단

## 코드 제시 방식

코드를 바로 던지는 방식으로 진행하지 않는다.

중요한 코드나 기술이 등장하면 최소한 다음을 연결한다.

```text
왜 필요한가
→ 현재 시스템에서 어떤 역할인가
→ 핵심 개념과 동작 원리
→ 코드가 실제로 어떻게 흐르는가
→ 주요 제약과 Trade-off
→ 구현
```

사용자가 이미 충분히 이해한 개념은 불필요하게 처음부터 반복하지 않는다.

반대로 현재 문제를 이해하는 핵심 개념이라면 이전에 한 번 배웠더라도 더 깊은 수준으로 다시 연결할 수 있다.

## 인프라 진행 방식

인프라도 명령어 복사·붙여넣기로 진행하지 않는다.

예를 들어 Nginx나 Load Balancer를 도입한다면 먼저 다음을 이해한다.

```text
현재 요청 경로는 무엇인가
왜 이 구성 요소가 필요한가
Reverse Proxy / L4 / L7의 의미
TCP / HTTP Connection은 어디서 맺히는가
Health Check와 Traffic Routing은 어떻게 동작하는가
장애 시 요청은 어떻게 되는가
Scale-out 후 병목은 어디로 이동하는가
```

그 뒤 실제 구성 명령을 실행한다.

Windows에서 사용자가 PowerShell을 사용하고 있다면 PowerShell 문법을 우선한다. 특정 Shell을 무조건 고정하지 않는다.

Codex는 현재 이 프로젝트의 기본 진행 도구로 사용하지 않는다.

---

# 14. 세션 인수인계 규칙

새 ChatGPT 세션에서 `large-scale-board`를 이어갈 때 다음 순서로 확인한다.

1. Wiki Home 또는 이 `PROJECT_HANDOFF.md`를 읽는다.
2. `README.md`의 최신 공식 로드맵을 읽는다.
3. GitHub에서 현재 `main` HEAD를 확인한다.
4. 현재 Phase Branch가 존재하는지 확인한다.
5. 열린 PR과 관련 Issue를 확인한다.
6. 현재 코드와 설정을 확인한다.
7. 과거 채팅만으로 local / branch / PR / CI 상태를 추측하지 않는다.
8. 현재 Phase에서 어떤 설계·구현·실험까지 끝났는지 확인한 뒤 이어간다.

동적 상태는 항상 다시 확인한다.

- main HEAD
- Phase branch HEAD
- PR 상태
- CI 상태
- Issue 상태
- 실제 merge 여부

반면 프로젝트 철학과 장기 운영 원칙은 Wiki / README를 Canonical Source로 사용한다.

---

# 15. 현재 다음 진행 지점

현재 실행 베이스라인 검증은 완료됐다.

```text
Client
  ↓
Spring Boot :8081
  ↓
HikariCP / JPA
  ↓
MySQL :3310
```

하지만 곧바로 Article CRUD를 시작하지 않는다.

**다음 학습은 Project Design Baseline부터 진행한다.**

권장 순서:

```text
프로젝트 목적 / Scope
→ Actor
→ 핵심 Use Case
→ Business Rule
→ Domain Map
→ 초기 ERD
→ API Contract 후보
→ 요청 / 데이터 흐름
→ 초기 Application / DB Architecture
→ NFR 후보
→ 부하 / 장애 가설
```

이 설계는 대규모 게시판이라는 단순한 도메인을 이해하고 이후 Phase의 설계 변화를 비교할 수 있을 정도로 구체적으로 만든다.

설계가 끝난 뒤 Phase 0의 시스템/JVM/Docker/MySQL/Actuator 기준선을 학습하고, Phase 1에서 실제 Article 중심 구현과 대규모 데이터 실험으로 들어간다.

---

# 16. 최종 지향점

초기 시스템:

```text
Client
→ Spring Boot
→ MySQL
```

이후 문제와 측정 결과에 의해 필요성이 확인되면 다음과 같은 구조까지 발전시킨다.

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

중요한 것은 이 구조를 빠르게 완성하는 것이 아니다.

**시스템을 직접 설계하고, 필요한 개념과 CS 원리를 이해하고, 문제를 재현하고, 측정하고, 대안을 비교하고, 선택을 검증하면서 단순한 시스템이 왜 이러한 구조로 발전하는지 스스로 설명할 수 있게 되는 것**이 프로젝트의 최종 목표다.
