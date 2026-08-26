# large-scale-board

대규모 데이터와 높은 트래픽을 처리하는 게시판 시스템을 직접 설계하고 구현하며, 성능 최적화와 분산 시스템 설계를 학습하는 실습 저장소다.

이 프로젝트는 기능을 빠르게 완성하거나 기술 스택을 많이 사용하는 것을 목표로 하지 않는다. 비교적 단순한 시스템에서 시작해 데이터와 트래픽을 증가시키고, 병목·동시성·데이터 정합성·장애 문제를 직접 재현한 뒤 여러 해결 방법을 비교하면서 시스템을 점진적으로 발전시키는 것이 핵심이다.

기존 Phase 0~7 로드맵을 **Part 1 — 대규모 단일 시스템**으로 유지하고, Part 1을 충분히 경험한 뒤 **Part 2 — 분산 시스템**으로 확장한다.

```text
Part 1. 대규모 단일 시스템

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

Part 2. 분산 시스템

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

처음부터 MSA를 만들지 않는다. 하나의 애플리케이션 안에서 충분히 문제를 경험한 뒤, **왜 서비스를 분리하는지와 분리했을 때 기존 트랜잭션·상태·장애 모델이 어떻게 달라지는지**를 직접 확인한다.

## 학습 목표

* 대규모 데이터와 트래픽이 단순 CRUD 시스템에 만드는 병목을 직접 재현한다.
* 요구사항과 장애 조건을 기준으로 기술을 선택하고 선택 근거를 설명할 수 있다.
* Scale-up과 Scale-out의 차이와 적용 기준을 이해한다.
* Monolithic Architecture와 Microservice Architecture의 장점과 비용을 비교한다.
* MySQL의 인덱스, 실행 계획, 트랜잭션, 락, 복제와 샤딩을 코드와 실험으로 검증한다.
* Snowflake 기반 분산 ID와 다양한 Primary Key 생성 전략을 비교한다.
* 계층형 댓글을 관계형 데이터베이스에 모델링하고 조회·정렬·삭제 전략을 비교한다.
* 높은 쓰기 트래픽에서 동시성과 데이터 일관성을 보장하는 방법을 구현하고 비교한다.
* Redis를 활용한 원자적 연산, 분산 상태 관리와 캐시 전략을 학습한다.
* Kafka 기반 이벤트 스트리밍과 비동기 처리를 구현하고 중복·순서·유실 문제를 다룬다.
* Transactional Outbox를 이용해 DB 변경과 이벤트 발행 사이의 일관성 문제를 개선한다.
* CQRS, 조회 모델, 비정규화와 캐시를 이용해 조회 경로를 최적화한다.
* 로그, 메트릭, 실행 계획과 부하 테스트를 이용해 병목 지점을 찾을 수 있다.
* 실행 계획, 처리량, 지연 시간, 오류율과 장애 복구 결과를 근거로 개선 효과를 판단한다.
* 성능 저하와 장애의 증상을 관찰하고 원인 후보를 좁혀 검증하는 과정을 익힌다.
* 모놀리스의 도메인 경계를 분석하고 Modular Monolith와 서비스 분리 기준을 설명할 수 있다.
* 서비스 분리 뒤 발생하는 네트워크 장애, 장애 전파와 분산 데이터 일관성 문제를 재현한다.
* Timeout, Retry, Circuit Breaker, Bulkhead와 Fallback의 필요성과 비용을 실험으로 비교한다.
* Multi-instance와 Load Balancer 환경에서 상태, 세션, 캐시와 커넥션의 문제를 확인한다.
* Metrics, Logs와 Trace를 함께 사용해 분산 시스템 장애의 원인을 추적할 수 있다.

## 학습 운영 원칙

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

1. 기술을 로드맵에 적혀 있다는 이유만으로 미리 도입하지 않는다.
2. 단순 구현이 실제로 어떤 한계를 만드는지 먼저 재현한다.
3. 성능 개선 전에는 반드시 현재 상태를 측정하고 병목의 근거를 확인한다.
4. 하나의 실험에서는 가능한 한 하나의 요소만 변경하고 동일 조건으로 다시 측정한다.
5. 하나의 문제에 여러 해결 방법을 적용하여 처리량, 지연, 충돌, 재시도와 복잡성을 비교할 수 있다.
6. 대량 데이터, 동시 요청, 장애와 데이터 불일치를 의도적으로 만든다.
7. 정상 상황뿐 아니라 데이터 유실, 중복, 지연과 복구까지 확인한다.
8. 학습 프로젝트이므로 데이터베이스, 캐시, 메시징, 관측, 인프라 등 다양한 기술을 폭넓게 실험한다.
9. 기술 사용 자체보다 왜 필요했고 어떤 비용과 새로운 실패 지점이 추가됐는지를 설명할 수 있어야 한다.
10. 모놀리스에서 경험한 문제를 이후 분산 시스템에서 다시 발생시켜 차이를 비교한다.
11. 블로그 글이나 포트폴리오 문서 작성은 완료 조건에 포함하지 않는다.
12. 실패한 실험과 기대와 달랐던 결과도 학습 결과로 취급한다.

## 참고 자료 사용 방식

[인프런 - 스프링부트로 직접 만들면서 배우는 대규모 시스템 설계 - 게시판](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8%EB%A1%9C-%EB%8C%80%EA%B7%9C%EB%AA%A8-%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%84%A4%EA%B3%84-%EA%B2%8C%EC%8B%9C%ED%8C%90?cid=334365)의 공개 목차는 학습 주제와 기술 범위를 참고하는 자료로 사용한다.

강의를 구매해 섹션 순서대로 따라가는 프로젝트가 아니다. 실제 진행 순서는 이 README의 로드맵과 현재 시스템에서 재현된 문제를 기준으로 한다. 강의 목차 외에도 공식 문서, 기술 서적, 국내외 기술 블로그, 다른 시스템 설계 프로젝트와 장애 사례를 적극적으로 참고한다.

## 현재 베이스라인

현재 코드는 새 로드맵을 처음부터 경험하기 위한 최소 실행 환경만 유지한다.

* Java 21
* Spring Boot 4.1.0
* Gradle 9.5.1
* Groovy DSL
* Spring Web MVC
* Spring Data JPA
* Hibernate
* Bean Validation
* MySQL 8.4
* Docker / Docker Compose
* Spring Boot Actuator
* Micrometer
* Testcontainers
* JUnit 5
* AssertJ
* Lombok

현재는 비즈니스 도메인, Spring Security, Flyway, Redis, Kafka, CQRS, 분산 락 등을 미리 구현하지 않는다.

Flyway는 현재 베이스라인에서 제거한다. 초기에는 구조를 빠르게 바꾸며 실험하기 위해 JPA 스키마 생성을 사용한다. 이후 스키마 변경과 배포, 여러 애플리케이션 버전의 공존 같은 문제가 실제 학습 주제로 등장하면 Flyway 또는 다른 Schema Migration 도구의 도입을 다시 검토할 수 있다.

## 관측과 성능 검증 운영 기준

모니터링과 성능 측정은 로드맵 마지막에 한 번 수행하는 작업이 아니라 각 기능의 문제를 발견하고 개선 효과를 검증하기 위한 핵심 학습 과정에 포함한다.

### 기본적으로 계속 사용하는 관측 도구

* Spring Boot Actuator
* Micrometer 기본 메트릭
* HTTP 요청 처리 시간과 상태 코드
* JVM 메모리, GC, 스레드와 CPU 메트릭
* HikariCP 활성·유휴·대기 커넥션
* 애플리케이션 로그와 요청 처리 시간 로그
* 실행된 SQL과 쿼리 횟수
* MySQL `EXPLAIN ANALYZE`
* MySQL Slow Query Log
* 락 대기와 트랜잭션 상태
* 기능별 커스텀 메트릭
* k6 기반의 짧고 반복 가능한 부하 테스트

기능별 실험은 다음 흐름을 기준으로 한다.

```text
기능 구현
→ 기준 데이터 구성
→ 부하 발생
→ 응답 시간·처리량·오류율 측정
→ 로그·메트릭·SQL·실행 계획 확인
→ 병목 후보 선정
→ 원인 검증
→ 하나의 요소 개선
→ 동일한 조건으로 재측정
→ 결과와 트레이드오프 해석
```

### 로드맵 중후반에 추가하는 통합 관측 환경

Phase 3과 Phase 4부터 여러 단계에서 수집한 메트릭을 시간 흐름에 따라 비교하기 위해 다음 도구를 추가한다.

* Prometheus
* Grafana
* Spring Boot Actuator Prometheus Endpoint
* 애플리케이션 커스텀 메트릭 대시보드
* JVM·HTTP·HikariCP·Redis·Kafka 메트릭 대시보드

Prometheus와 Grafana는 병목을 자동으로 찾아 주는 도구가 아니다. 로그, SQL, 실행 계획과 메트릭을 함께 분석하기 위한 수집·시각화 도구로 사용한다.

---

# Part 1. 대규모 단일 시스템

기존 Phase 0~7 로드맵을 유지한다. 하나의 Spring Boot 애플리케이션을 출발점으로 데이터베이스, 동시성, Redis, Kafka, CQRS와 캐시를 단계적으로 경험한다.

## Phase 0. 시스템 기반과 개발 환경

### 학습 및 실습

* 대규모 시스템의 기준과 주요 병목 지점
* Scale-up과 Scale-out
* Load Balancer와 요청 분산
* Stateful 서버와 Stateless 서버
* 애플리케이션 서버 수평 확장
* 데이터베이스가 병목이 되는 이유
* Monolithic Architecture
* Microservice Architecture
* 서비스 분리가 만드는 네트워크와 데이터 일관성 문제
* Docker 이미지와 컨테이너
* Docker 네트워크와 볼륨
* Docker Compose 기반 로컬 인프라 구성
* MySQL 개발 환경 구성
* Spring Boot 프로젝트와 패키지 구조 설정
* JPA, Validation과 Actuator 기본 구성
* Testcontainers 기반 통합 테스트 환경 구성
* 여러 애플리케이션 인스턴스를 실행할 수 있는 기반 이해
* 초기 JPA Schema 생성 방식과 향후 명시적 Schema 관리 방식 비교

### 관측 및 성능 검증

* Actuator Health Endpoint 확인
* Actuator Metrics Endpoint 확인
* JVM 메모리와 GC 메트릭 확인
* HikariCP 커넥션 풀 메트릭 확인
* HTTP 요청 처리 시간 메트릭 확인
* 요청별 처리 시간을 남기는 기본 로그 구성
* 애플리케이션 시작과 종료 로그 확인
* MySQL 연결 상태와 커넥션 풀 초기화 확인
* 이후 성능 비교에 사용할 기준 환경 기록

### 완료 조건

* 서버 확장 방식과 상태 공유 문제를 설명할 수 있다.
* 모놀리식과 마이크로서비스 구조의 장점과 비용을 비교할 수 있다.
* Docker Compose로 실습 인프라를 실행하고 종료할 수 있다.
* Spring Boot 애플리케이션과 MySQL 연결을 테스트로 검증할 수 있다.
* JPA의 Schema 생성 옵션이 개발 편의성과 데이터 보존에 미치는 영향을 설명할 수 있다.
* Actuator를 이용해 애플리케이션과 커넥션 풀의 기본 상태를 확인할 수 있다.
* 로그와 메트릭이 성능 문제를 찾는 데 어떤 역할을 하는지 설명할 수 있다.

---

## Phase 1. 게시글과 분산 데이터베이스

### 학습 및 실습

* 게시글 API와 데이터 모델 설계
* 게시글 생성·조회·수정·삭제
* Request DTO와 Response DTO 분리
* Bean Validation을 이용한 요청 검증
* 인증 사용자가 필요해지는 시점과 인증 도입 범위 판단
* 작성자와 관리자 권한이 필요한 경우의 권한 검증
* 전역 예외 처리와 일관된 오류 응답
* 서비스 계층의 트랜잭션 경계
* DDL과 SQL을 이용한 테이블·인덱스 변경 실험
* Auto Increment 기반 ID
* UUID 기반 ID
* Snowflake 기반 분산 ID
* Snowflake 서버 ID 충돌 실험
* 게시글 10만 건 이상 테스트 데이터 생성
* 게시글 100만 건 이상 테스트 데이터 생성
* OFFSET 기반 페이지네이션
* 커서 기반 페이지네이션
* 전체 게시글 수 조회
* `COUNT(*)` 비용 분석
* Clustered Index
* Secondary Index
* Covering Index
* 복합 인덱스의 컬럼 순서
* `EXPLAIN ANALYZE`를 이용한 실행 계획 분석
* 인덱스 적용 전후 성능 비교
* 단일 DB의 읽기·쓰기 한계 측정
* Primary / Replica와 Read Scaling
* 복제 지연과 Read-after-write 문제
* 다중 MySQL 인스턴스를 이용한 논리적 샤딩
* 샤드 키 선정
* 해시 샤딩과 범위 샤딩
* 데이터 편중과 Hot Shard
* 여러 샤드의 게시글 목록 병합
* Cross-Shard Query
* 샤드 간 전체 개수 조회
* 샤드 일부 장애 실험

### 관측 및 성능 검증

* API별 요청 수와 처리 시간 확인
* 평균 응답 시간과 p50·p95·p99 비교
* HTTP 오류율 확인
* 실행 SQL과 쿼리 횟수 확인
* N+1 문제 탐지
* `EXPLAIN ANALYZE` 결과 분석
* 실제 읽은 레코드 수 확인
* 인덱스 사용 여부 확인
* HikariCP 활성·유휴·대기 커넥션 확인
* MySQL Slow Query Log 확인
* k6 기반 게시글 목록 조회 부하 테스트
* 데이터 수 증가에 따른 응답 시간 변화 측정
* OFFSET 깊이에 따른 성능 변화 측정
* 인덱스 적용 전후 동일 조건 비교
* Primary / Replica 도입 전후 읽기 처리량과 복제 지연 확인
* 샤딩 적용 전후 처리량과 복잡성 비교

### 완료 조건

* 분산 환경에서 별도의 ID 생성 전략이 필요한 이유를 설명할 수 있다.
* Auto Increment, UUID와 Snowflake의 장단점을 비교할 수 있다.
* OFFSET과 커서 기반 페이지네이션의 성능 차이를 실험 결과로 설명할 수 있다.
* 쿼리 조건과 정렬에 맞는 인덱스를 설계할 수 있다.
* 실행 계획을 읽고 병목이 되는 구간을 찾을 수 있다.
* Replica가 해결하는 문제와 복제 지연이 만드는 문제를 설명할 수 있다.
* 샤딩이 해결하는 문제와 새로 만드는 문제를 설명할 수 있다.
* Hot Shard와 Cross-Shard Query가 발생하는 이유를 설명할 수 있다.
* HTTP 메트릭, SQL과 실행 계획을 함께 이용해 병목 원인을 좁힐 수 있다.
* 성능 개선 전후 결과를 동일한 조건으로 비교할 수 있다.

---

## Phase 2. 계층형 댓글

### 학습 및 실습

* 댓글과 답글 요구사항 정의
* 최대 2-depth 댓글 모델링
* 무한-depth 댓글 모델링
* 자기 참조 테이블
* Adjacency List
* Path Enumeration
* Materialized Path
* Closure Table 비교
* 댓글 생성·수정·삭제
* 댓글과 답글 목록 조회
* 경로 기반 댓글 정렬
* 특정 댓글의 하위 트리 조회
* 데이터베이스 Collation이 경로 정렬에 미치는 영향
* 부모 댓글 삭제와 자식 댓글 유지 정책
* 논리 삭제와 물리 삭제
* 깊은 댓글 트리 생성
* 특정 부모에게 댓글이 몰리는 편중 실험
* 모델별 읽기·쓰기 비용 비교

### 관측 및 성능 검증

* 댓글 모델별 INSERT 쿼리 횟수 비교
* 댓글 모델별 SELECT 쿼리 횟수 비교
* 댓글 깊이에 따른 조회 시간 변화 측정
* 댓글 개수에 따른 p95 응답 시간 변화 측정
* 경로 정렬 쿼리의 실행 계획 분석
* 하위 트리 조회 시 읽은 레코드 수 확인
* 부모 삭제 시 영향받는 레코드 수 확인
* 깊은 트리와 넓은 트리의 성능 비교
* 모델별 읽기 비용과 쓰기 비용 수치화

### 완료 조건

* 댓글 깊이 요구사항에 맞는 데이터 모델을 선택할 수 있다.
* 2-depth와 무한-depth 댓글의 설계 차이를 설명할 수 있다.
* 계층형 데이터의 조회·정렬·삭제 전략을 설명할 수 있다.
* 경로 기반 댓글 정렬과 하위 트리 조회를 구현할 수 있다.
* 각 모델의 읽기 비용과 쓰기 비용을 비교할 수 있다.
* 댓글 구조에 따른 쿼리 비용 차이를 실행 결과로 설명할 수 있다.

---

## Phase 3. 좋아요와 동시성

### 학습 및 실습

* 좋아요 생성과 취소
* 동일 사용자의 중복 좋아요 처리
* `(article_id, member_id)` Unique Constraint
* 애플리케이션 중복 검증의 한계
* 조회 후 증가 방식에서 발생하는 Lost Update
* 원자적 UPDATE
* 트랜잭션 격리 수준
* Record Lock
* Optimistic Lock
* Pessimistic Lock
* 멱등성과 재시도
* 여러 스레드를 이용한 동시성 테스트
* 여러 애플리케이션 인스턴스에서의 동시 요청
* 애플리케이션 내부 락의 한계
* Redis Distributed Lock
* Redis Lua 기반 원자 처리
* 데이터베이스 락과 분산 락 비교
* 방식별 성공률, 처리량과 응답 시간 비교

동일한 문제에 가능한 경우 여러 전략을 적용한다.

```text
Atomic Update
vs
Optimistic Lock
vs
Pessimistic Lock
vs
Redis Distributed Lock
```

### 관측 및 성능 검증

* 기대한 최종 좋아요 수와 실제 값 비교
* 요청 성공·실패·충돌 횟수 측정
* Optimistic Lock 재시도 횟수 측정
* DB 락 대기 시간 확인
* 방식별 초당 처리량 비교
* 방식별 p95와 p99 응답 시간 비교
* 동시 사용자 증가에 따른 오류율 확인
* 커넥션 풀 대기 증가 여부 확인
* 좋아요 성공·충돌·재시도 커스텀 메트릭 추가
* k6 기반 동시 좋아요 시나리오 실행
* Prometheus와 Grafana 기본 환경 도입
* HTTP·JVM·HikariCP 메트릭 대시보드 구성

### 완료 조건

* 애플리케이션 검증과 데이터베이스 제약 조건의 역할을 구분할 수 있다.
* Lost Update를 재현하고 발생 원인을 설명할 수 있다.
* 원자적 UPDATE가 동시성 문제를 해결하는 원리를 설명할 수 있다.
* 낙관적 락과 비관적 락의 적합한 상황을 비교할 수 있다.
* 단일 서버 락이 분산 환경을 보호하지 못하는 이유를 설명할 수 있다.
* 분산 락이 항상 최선의 해결책은 아닌 이유를 설명할 수 있다.
* 충돌과 재시도 메트릭을 이용해 동시성 제어 방식의 비용을 비교할 수 있다.
* Grafana에서 HTTP, JVM과 커넥션 풀의 변화를 함께 확인할 수 있다.

---

## Phase 4. 조회수와 높은 쓰기 트래픽

### 학습 및 실습

* MySQL 직접 증가 방식
* 동일 게시글에 집중되는 높은 쓰기 트래픽
* Hot Row 문제
* 조회수 증가 동시성 테스트
* Redis 자료구조
* Redis `INCR` 원자 연산
* 사용자 또는 쿠키 기반 중복 조회 방지
* 정확한 조회수와 근사 조회수
* Write-through
* Write-back
* Redis 조회수의 MySQL 배치 반영
* Batch Flush
* 중복 반영 방지
* 동기화 중 애플리케이션 종료
* Redis 장애 실험
* 반영되지 않은 조회수의 유실 범위 확인
* 조회수 복구 전략
* MySQL 직접 증가 방식과 Redis 방식의 처리량 비교

### 관측 및 성능 검증

* MySQL 초당 UPDATE 횟수 확인
* 동일 레코드 락 대기 확인
* 조회수 증가 API 처리량 측정
* MySQL 방식의 p95와 p99 측정
* Redis `INCR` 방식의 p95와 p99 측정
* Redis 명령 처리량 확인
* Redis 연결 상태 확인
* DB 반영 대기 조회수 측정
* 마지막 동기화 이후 지연 시간 측정
* 배치 반영 성공·실패 횟수 측정
* 반영되지 않은 조회수의 개수 측정
* 조회수 증가·동기화·실패 커스텀 메트릭 추가
* Grafana에 Redis와 조회수 동기화 대시보드 추가
* MySQL 직접 증가 방식과 Redis 방식의 동일 조건 비교

### 완료 조건

* 조회마다 DB UPDATE를 수행할 때 발생하는 병목을 설명할 수 있다.
* Hot Row가 발생하는 원인을 설명할 수 있다.
* Redis `INCR`의 원자성을 설명할 수 있다.
* Write-through와 Write-back의 차이를 설명할 수 있다.
* 성능과 데이터 정확성 사이의 트레이드오프를 판단할 수 있다.
* Redis 장애 또는 애플리케이션 종료 시 데이터 유실 범위를 설명할 수 있다.
* Grafana 메트릭을 이용해 DB 락 경합과 Redis 방식의 차이를 비교할 수 있다.
* 조회수 동기화 지연과 실패 상태를 메트릭으로 확인할 수 있다.

---

## Phase 5. 인기글과 이벤트 기반 아키텍처

### 학습 및 실습

* 인기 점수 계산 기준
* 기간별 인기글 요구사항
* 동기 처리와 비동기 처리
* Event-Driven Architecture
* Kafka Topic
* Kafka Partition
* Kafka Offset
* Consumer Group
* Partition 단위 순서 보장
* At-least-once 전달
* 이벤트 중복 소비
* 게시글 이벤트
* 댓글 이벤트
* 좋아요 이벤트
* 조회 이벤트
* 이벤트 기반 인기글 집계
* 멱등 Consumer
* Consumer 재처리
* Retry / Backoff
* Dead Letter Queue
* DB 커밋 후 Kafka 발행 실패 재현
* Kafka 발행 후 애플리케이션 장애 실험
* Distributed Transaction
* Two-Phase Commit
* Transactional Outbox
* Outbox Relay
* CDC
* Transaction Log Tailing
* Outbox 이벤트 중복 발행
* Producer 장애
* Consumer 장애
* Kafka 장애
* 이벤트 적체와 복구 실험

### 관측 및 성능 검증

* 이벤트 생성 횟수 측정
* 이벤트 발행 성공·실패 횟수 측정
* 이벤트 소비 성공·실패 횟수 측정
* 중복 이벤트 처리 횟수 측정
* Consumer 재시도 횟수 측정
* Kafka Consumer Lag 확인
* Partition별 처리량과 지연 확인
* Outbox 대기 이벤트 수 측정
* 가장 오래된 Outbox 이벤트의 대기 시간 측정
* 이벤트 생성부터 인기글 반영까지 걸린 시간 측정
* Producer·Consumer·Outbox 커스텀 메트릭 추가
* Kafka와 Outbox Grafana 대시보드 구성
* Consumer 중단 전후 Lag 변화 확인
* 장애 복구 후 적체 해소 시간 측정

### 완료 조건

* Kafka의 Topic, Partition, Offset과 Consumer Group의 관계를 설명할 수 있다.
* Kafka가 순서를 보장하는 범위를 설명할 수 있다.
* 이벤트 유실과 이벤트 중복을 별개의 문제로 다룰 수 있다.
* Consumer를 멱등하게 구현해야 하는 이유를 설명할 수 있다.
* DB 변경과 이벤트 발행 사이의 원자성 문제를 재현할 수 있다.
* Transactional Outbox가 해결하는 문제와 한계를 설명할 수 있다.
* Consumer Lag과 Outbox 적체 메트릭을 이용해 처리 지연을 탐지할 수 있다.
* 장애 발생부터 적체 복구까지의 과정을 지표로 설명할 수 있다.

---

## Phase 6. 게시글 조회 최적화

### 학습 및 실습

* 게시글 상세 조회에 필요한 도메인 데이터 조합
* 게시글, 댓글 수, 좋아요 수와 조회수 조회
* 복잡한 Join과 Query 비용
* CQRS
* Command Model
* Read Model
* 비정규화된 조회 전용 데이터
* 이벤트 기반 조회 모델 갱신
* 원본 데이터와 조회 모델 사이의 Eventual Consistency
* 조회 모델 갱신 지연
* Cache Aside
* Cache Hit와 Cache Miss
* TTL
* 캐시 무효화
* 캐시 삭제 실패
* Cache Stampede
* Hot Key
* Request Collapsing
* Redis 장애 시 DB Fallback
* 캐시와 DB 데이터 불일치
* 조회 모델과 원본 데이터의 불일치
* 캐시 적용 전후 응답 시간과 DB 부하 비교

### 관측 및 성능 검증

* 게시글 상세 API 전체 처리 시간 측정
* 내부 데이터 조회 단계별 처리 시간 측정
* 캐시 적중 횟수 측정
* 캐시 실패 횟수 측정
* 캐시 적중률 계산
* DB Fallback 횟수 측정
* 조회 모델 갱신 지연 시간 측정
* 캐시 적용 전후 DB 쿼리 횟수 비교
* 캐시 적용 전후 HikariCP 사용량 비교
* 캐시 적용 전후 p95와 p99 비교
* Cache Stampede 발생 시 DB 부하 확인
* Request Collapsing 적용 전후 DB 호출 수 비교
* Redis 장애 시 오류율과 응답 시간 확인
* 캐시·조회 모델 커스텀 메트릭 추가
* Grafana 통합 조회 대시보드 구성

### 완료 조건

* CQRS를 단순한 코드 계층 분리가 아닌 데이터 모델 분리 관점에서 설명할 수 있다.
* 조회 모델을 별도로 구성하는 이유를 설명할 수 있다.
* 비정규화된 데이터의 성능 이점과 갱신 비용을 비교할 수 있다.
* Cache Aside의 처리 흐름을 구현할 수 있다.
* 캐시 무효화 실패가 만드는 문제를 설명할 수 있다.
* Cache Stampede와 Hot Key를 구분할 수 있다.
* Redis 장애가 발생해도 핵심 조회 기능이 동작하도록 설계할 수 있다.
* 캐시 적중률과 DB Fallback 메트릭을 이용해 캐시 효과를 판단할 수 있다.
* 조회 모델 갱신 지연과 원본 데이터 불일치 범위를 측정할 수 있다.

---

## Phase 7. 전체 시스템 통합과 검증

### 학습 및 실습

* 전체 요청 흐름 점검
* 전체 데이터 흐름 점검
* 기술별 도입 이유와 트레이드오프 점검
* 기능 정확성 검증
* 성능 개선 결과 검증
* 데이터 정합성 검증
* 장애 대응 결과 검증
* MySQL 장애 실험
* Redis 장애 실험
* Kafka 장애 실험
* Consumer 중단 실험
* Outbox Relay 중단 실험
* 이벤트 중복 처리 실험
* 이벤트 처리 지연 실험
* 조회 모델 갱신 지연 실험
* 캐시와 DB 불일치 실험
* 샤드 일부 장애 실험
* 남아 있는 한계 정리
* Part 2에서 분산 시스템으로 확장할 후보와 이유 정리

### 관측 및 성능 검증

* 전체 API 요청 수와 오류율 확인
* API별 p50·p95·p99 비교
* JVM, HTTP, HikariCP, MySQL, Redis와 Kafka 지표 통합
* Phase별 성능 개선 결과 비교
* 초기 단순 구조와 최종 구조의 처리량 비교
* 기술 도입 전후 응답 시간 비교
* 장애 발생 시점과 메트릭 변화 연결
* 장애 복구에 걸린 시간 측정
* 데이터 유실·중복·지연 건수 확인
* 이벤트 적체와 복구 흐름 확인
* 캐시와 조회 모델의 정합성 점검
* 전체 k6 통합 부하 테스트
* 짧은 반복 부하 테스트 결과의 재현성 검증
* Grafana 통합 대시보드 완성
* 현재 구조에서 남은 병목과 관측 공백 확인

Part 1의 최종 데이터 흐름 예시는 다음과 같다.

```text
HTTP 요청
→ Spring Boot 애플리케이션
→ MySQL 상태 변경과 Outbox 저장
→ Kafka 이벤트 발행
→ Consumer 처리
→ 인기글과 조회 모델 갱신
→ Redis 캐시 또는 카운터 반영
→ 사용자 조회 응답
```

### Part 1 완료 조건

* 각 기술을 사용하는 이유를 문제 상황과 연결해 설명할 수 있다.
* 구현 코드의 요청 흐름과 트랜잭션 경계를 설명할 수 있다.
* 테스트와 측정 결과로 기능 정확성과 성능 개선을 증명할 수 있다.
* 로그, 메트릭, SQL과 실행 계획을 이용해 병목 후보를 찾을 수 있다.
* 평균 응답 시간뿐 아니라 처리량, p95, p99와 오류율을 함께 해석할 수 있다.
* 장애 발생 시 데이터의 유실·중복·지연 범위를 설명할 수 있다.
* 장애가 자동으로 복구되는 구간과 수동 복구가 필요한 구간을 구분할 수 있다.
* 선택하지 않은 대안과 현재 설계의 한계를 설명할 수 있다.
* Grafana 대시보드의 이상 지표를 실제 코드와 데이터 흐름에 연결해 분석할 수 있다.
* 개선 전후 결과를 동일한 조건으로 비교하고 결론을 설명할 수 있다.
* Part 2에서 어떤 경계를 왜 분리할 것인지 근거를 제시할 수 있다.

---

# Part 2. 분산 시스템

Part 1을 모두 경험한 뒤 기존 모놀리스를 실제 분산 시스템으로 발전시킨다. 최종 그림을 먼저 만들지 않고, 분리로 얻는 이점과 새롭게 생기는 비용을 단계마다 확인한다.

## Phase 8. Modular Monolith와 서비스 경계

바로 MSA로 분리하지 않는다. 기존 시스템의 도메인 관계와 트랜잭션 경계를 분석하고 먼저 모듈 경계를 명확하게 만든다.

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

### 학습 및 실습

* 도메인 간 의존 관계 분석
* Transaction Boundary 분석
* DB FK와 강한 데이터 결합 확인
* 패키지·모듈 간 순환 의존성 확인
* 다른 도메인의 내부 모델 직접 참조 확인
* Public API와 내부 구현 경계 구분
* Modular Monolith 구조 실험
* 동기 호출을 유지해야 하는 경계와 이벤트로 바꿀 수 있는 경계 구분
* 서비스 분리 후보와 분리하지 않을 후보 비교

### 관측 및 검증

* 모듈 간 의존 방향 시각화
* 주요 Use Case별 트랜잭션 경계 기록
* 도메인 변경이 다른 모듈에 미치는 영향 확인
* 분리 후보별 동기 호출 횟수와 데이터 의존성 확인

### 완료 조건

* 기능 단위가 아니라 데이터 소유권과 변경 이유를 기준으로 경계를 설명할 수 있다.
* 모듈과 서비스의 차이를 설명할 수 있다.
* 어떤 경계를 분리하지 않는 것이 더 나은지도 설명할 수 있다.
* 서비스 분리 전에 Modular Monolith가 제공하는 장점을 설명할 수 있다.

---

## Phase 9. Microservice 분리

Phase 8에서 확인한 경계 중 일부만 실제 독립 프로세스로 분리한다.

후보:

```text
Article Service
Engagement Service
Ranking Service
Query Service
```

### 학습 및 실습

* 일부 도메인의 독립 Spring Boot 애플리케이션 분리
* Database per Service
* 데이터 소유권 분리
* 서비스 간 FK 제거
* 독립 배포와 버전 관리
* 기존 단일 `@Transactional` 경계의 붕괴 확인
* 서비스별 설정과 리소스 격리
* 분리 전후 코드 복잡성, 배포 단위와 장애 범위 비교

### 완료 조건

* Database per Service가 필요한 이유와 비용을 설명할 수 있다.
* 서비스 분리로 기존 트랜잭션이 왜 유지되지 않는지 설명할 수 있다.
* 프로세스 분리가 코드 모듈 분리보다 어떤 추가 복잡성을 만드는지 설명할 수 있다.

---

## Phase 10. 서비스 간 동기 통신과 장애 전파

분리된 서비스끼리 HTTP 기반 동기 통신을 사용한다. RestClient, WebClient, OpenFeign 등은 구현 후보이며 라이브러리 자체보다 네트워크 실패 모델이 핵심이다.

### 문제 재현

```text
Query Service
→ Article Service
→ 응답 지연
→ Thread 대기
→ 요청 적체
→ 전체 API 지연
```

### 학습 및 실습

* Connection Refused
* Slow Response
* Timeout
* Retry
* Exponential Backoff와 Jitter
* Retry Storm
* Connection Pool 고갈
* 장애 전파
* Timeout 설정
* Circuit Breaker
* Bulkhead
* Fallback
* 동기 호출 체인 길이에 따른 지연 증가

### 관측 및 성능 검증

* 서비스별 응답 시간
* Connection Pool 사용량
* Thread 대기 수
* Timeout 횟수
* Retry 횟수
* Circuit Breaker 상태 변화
* 장애 서비스와 호출 서비스의 p95/p99 변화
* Retry 적용 전후 총 부하 비교

### 완료 조건

* Timeout 없이 원격 호출을 사용하는 위험을 설명할 수 있다.
* Retry가 장애를 악화시킬 수 있는 이유를 설명할 수 있다.
* Circuit Breaker와 Bulkhead가 해결하는 문제가 어떻게 다른지 설명할 수 있다.
* 동기 호출이 서비스 간 결합과 장애 전파에 미치는 영향을 지표로 설명할 수 있다.

---

## Phase 11. 분산 데이터 일관성

서비스가 분리된 뒤 하나의 데이터베이스 트랜잭션으로 처리할 수 없는 상황을 직접 만든다.

예시:

```text
Article 삭제
     ↓
ArticleDeleted Event
     ↓
Kafka
 ┌───┴──────────┐
 ▼              ▼
Comment      Engagement
Cleanup        Cleanup
```

### 학습 및 실습

* Event Publish 실패
* Consumer 장애
* Duplicate Event
* Out-of-order Event
* 부분 처리 실패
* Producer 재시작
* Kafka 장애
* Eventual Consistency
* Transactional Outbox
* Idempotent Consumer
* Retry
* DLQ
* Event Versioning
* Compensation
* Saga

Saga는 이름을 배우기 위해 적용하지 않는다. 여러 서비스의 비즈니스 상태 변경에서 실제 보상 트랜잭션이 필요할 때 적용한다.

### 관측 및 검증

* 서비스별 최종 데이터 불일치 시간
* 이벤트 처리 지연
* 중복 처리 횟수
* 보상 처리 성공·실패 횟수
* DLQ 적체
* Saga 단계별 상태와 실패 지점

### 완료 조건

* 로컬 트랜잭션과 분산 비즈니스 트랜잭션의 차이를 설명할 수 있다.
* Eventual Consistency를 허용할 수 있는 범위를 판단할 수 있다.
* 보상 가능한 작업과 보상하기 어려운 작업을 구분할 수 있다.
* Saga를 적용해야 하는 실제 조건을 설명할 수 있다.

---

## Phase 12. Multi-instance와 Load Balancing

서비스를 여러 인스턴스로 실행하고 요청을 분산한다.

```text
              Load Balancer
               /        \
              /          \
        Article #1    Article #2
              \          /
                  DB
```

### 학습 및 실습

* Local Cache 불일치
* Local State
* Session 문제
* Sticky Session
* Shared Session
* Stateless 구조
* Health Check
* Instance Failure
* Graceful Shutdown
* Scale-out
* Redis Connection 증가
* DB Connection 증가
* Kafka Consumer Group 재조정

### 관측 및 성능 검증

* 인스턴스별 요청 분포
* Scale-out 전후 TPS와 p95/p99
* 인스턴스별 CPU와 Heap
* 전체 DB Connection 수
* Redis Connection 수
* Kafka Consumer Rebalance
* 인스턴스 종료 중 오류율
* Health Check 실패 후 트래픽 제거 시간

### 완료 조건

* 애플리케이션을 늘리는 것이 무조건 처리량 증가로 이어지지 않는 이유를 설명할 수 있다.
* Scale-out 후 병목이 DB, Redis 또는 Kafka로 이동하는 현상을 설명할 수 있다.
* Sticky Session, Shared Session과 Stateless 방식의 비용을 비교할 수 있다.
* Graceful Shutdown이 필요한 이유를 실제 요청 흐름으로 설명할 수 있다.

---

## Phase 13. Distributed Observability와 장애 실험

서비스가 여러 개가 된 뒤 하나의 요청이 어디에서 느려졌는지 파악하기 어려운 문제를 경험한다.

후보 기술:

```text
Metrics → Prometheus
Logs    → Loki
Trace   → OpenTelemetry / Tempo
View    → Grafana
```

예시 흐름:

```text
Client
→ Query Service
→ Article Service
→ DB

        +

→ Engagement Service
→ Redis
```

### 학습 및 실습

* 서비스 공통 Correlation ID / Trace ID
* OpenTelemetry 기반 Trace 수집
* 서비스 간 Context Propagation
* Metric, Log와 Trace 연계
* 특정 서비스 Slow Response
* 특정 Instance Down
* Redis Down
* DB Down
* Kafka Consumer Down
* Kafka Lag 증가
* Network Failure
* Read Model 지연

### 관측 및 성능 검증

* End-to-End 요청 시간
* 서비스별 Span 시간
* 서비스별 Error Rate
* Trace와 Log 연결
* Kafka Lag과 요청 결과의 관계
* DB/Redis 장애 시 의존 서비스 영향 범위
* 장애 탐지 시간과 원인 규명 시간

### 완료 조건

* 단일 애플리케이션 관측과 분산 추적의 차이를 설명할 수 있다.
* 하나의 사용자 요청을 여러 서비스의 Trace로 따라갈 수 있다.
* Metrics만으로 판단하기 어려운 문제를 Logs와 Trace로 좁힐 수 있다.
* 분산 장애가 어떤 의존 경로를 통해 전파됐는지 설명할 수 있다.

---

# 최종적으로 경험할 시스템

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

최종 그림 자체가 목표가 아니다. 각 구성 요소가 없었을 때의 문제를 먼저 경험하고, 도입 후 성능과 정확성뿐 아니라 새롭게 생긴 운영·장애 비용까지 확인하는 과정이 목표다.

## 실험 원칙

* 처음부터 최적화된 구조를 적용하지 않고 단순 구현의 한계를 먼저 재현한다.
* 기술을 추가하기 전에 해결하려는 문제를 명확하게 정의한다.
* 성능 개선 전에는 현재 처리량, 응답 시간, 오류율과 자원 상태를 측정한다.
* 성능 개선 전후에는 동일한 데이터와 실험 조건을 사용한다.
* 하나의 실험에서는 가능한 한 하나의 요소만 변경한다.
* 한 번의 실행 결과만으로 결론을 내리지 않는다.
* 평균 응답 시간뿐 아니라 처리량과 p50·p95·p99 지연 시간을 함께 확인한다.
* 데이터베이스 실험에서는 실행 계획, 실제 실행 시간과 읽은 레코드 수를 확인한다.
* 커넥션 풀 실험에서는 활성 커넥션과 대기 요청 수를 함께 확인한다.
* 동시성 테스트에서는 기대한 최종 값과 실제 값을 비교한다.
* 비동기 처리 실험에서는 처리량뿐 아니라 Lag과 최종 반영 지연을 확인한다.
* 캐시 실험에서는 응답 시간뿐 아니라 적중률과 DB 부하를 함께 확인한다.
* 장애 테스트에서는 데이터 유실, 중복, 지연과 재처리 가능 여부를 확인한다.
* 분산 시스템 실험에서는 네트워크 실패와 부분 실패를 정상적인 실패 모델로 취급한다.
* 메트릭의 변화만 보고 원인을 확정하지 않고 로그, SQL, Trace와 코드 흐름으로 검증한다.
* Redis, Kafka, 샤딩과 MSA는 기술 스택을 늘리기 위한 장식이 아니라 문제 해결 수단으로 사용한다.
* 개선 결과가 기대와 다르면 실패 원인도 학습 결과로 남긴다.

## 현재 진행 상태

* [x] Spring Initializr 기반 프로젝트 생성
* [x] Java 21과 Spring Boot 4.1.0 설정
* [x] 새 학습 로드맵을 위한 기존 비즈니스 코드 초기화
* [x] Flyway와 Spring Security를 초기 베이스라인에서 제거
* [x] Spring Web MVC, JPA, Validation, Actuator와 Testcontainers 최소 의존성 구성
* [x] Docker Compose 기반 MySQL 실행 환경 구성
* [ ] Phase 0. 최소 애플리케이션 실행과 MySQL 연결 재검증
* [ ] Phase 0. Actuator와 기본 관측 환경 확인
* [ ] Phase 0. 초기 성능·자원 기준선 기록
* [ ] Phase 1. 게시글과 분산 데이터베이스
* [ ] Phase 2. 계층형 댓글
* [ ] Phase 3. 좋아요와 동시성
* [ ] Phase 3. Prometheus와 Grafana 기본 환경 구성
* [ ] Phase 4. 조회수와 높은 쓰기 트래픽
* [ ] Phase 5. 인기글과 이벤트 기반 아키텍처
* [ ] Phase 6. 게시글 조회 최적화
* [ ] Phase 7. 전체 시스템 통합과 검증
* [ ] Phase 8. Modular Monolith와 서비스 경계
* [ ] Phase 9. Microservice 분리
* [ ] Phase 10. 서비스 간 동기 통신과 장애 전파
* [ ] Phase 11. 분산 데이터 일관성
* [ ] Phase 12. Multi-instance와 Load Balancing
* [ ] Phase 13. Distributed Observability와 장애 실험

# Part 2 이후 추가 확장 가능 범위

Phase 13 이후에도 학습 가치가 있다면 운영, 프로파일링, 배포와 플랫폼 영역으로 계속 확장한다.

## 운영 관측과 장애 탐지

* Prometheus 메트릭 보존 기간과 저장 구조 고도화
* Grafana 대시보드 운영용 재구성
* Alertmanager 기반 메트릭 알림
* 오류율, 응답 지연과 자원 고갈 임계치 설정
* Slack 또는 Discord 장애 알림
* 구조화 로그 표준화
* Loki 또는 별도 로그 저장소를 이용한 로그 중앙 수집
* OpenTelemetry 기반 Trace 수집 고도화
* Tempo 또는 별도 Trace 저장소
* Sentry 기반 예외 수집과 오류 그룹화
* 배포 버전별 오류 추적
* 메트릭·로그·Trace 연계 분석

## 프로파일링과 장시간 성능 검증

* Java Flight Recorder
* Java Mission Control
* CPU 프로파일링
* 메모리 할당과 누수 분석
* 스레드 덤프 분석
* Heap Dump 분석
* Soak Test
* Spike Test
* Stress Test
* 최대 처리량과 한계 지점 탐색
* 성능 저하 추세와 자원 누수 확인

## 운영과 배포

* 애플리케이션 장애 대응
* 인프라 장애 대응
* 장애 대응 Runbook
* 백업과 복구
* 데이터 정합성 점검
* CI/CD
* 배포 자동화
* 클라우드 배포
* 수평 확장
* 무중단 배포와 롤백
* Rate Limiting
* Circuit Breaker 고도화
* 운영 보안

## 인프라와 플랫폼 확장

필요성이 확인되면 다음도 학습 후보로 둔다.

* Nginx / Reverse Proxy
* L4 / L7 Load Balancing
* AWS ALB / NLB
* ECS / Kubernetes
* Service Discovery
* API Gateway
* Container Resource Limit
* Autoscaling
* Rolling / Blue-Green / Canary Deployment
* Chaos Engineering

모든 추가 기술 역시 **문제 재현 → 필요성 확인 → 도입 → 측정 → 장애 실험 → 트레이드오프 분석** 원칙을 유지한다.
