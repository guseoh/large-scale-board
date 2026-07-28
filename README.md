# large-scale-board

대규모 데이터와 높은 트래픽을 처리하는 게시판 시스템을 직접 설계하고 구현하며, 분산 시스템과 성능 최적화 기술을 학습하는 실습 저장소입니다.

이 프로젝트는 기능 완성이나 포트폴리오 제작보다 **기술이 필요한 문제를 직접 재현하고, 해결 방법의 동작 원리와 트레이드오프를 코드와 실험으로 이해하는 것**을 우선합니다.

> [인프런 - 스프링부트로 직접 만들면서 배우는 대규모 시스템 설계: 게시판](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8%EB%A1%9C-%EB%8C%80%EA%B7%9C%EB%AA%A8-%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%84%A4%EA%B3%84-%EA%B2%8C%EC%8B%9C%ED%8C%90?cid=334365)의 공개 커리큘럼과 기술 키워드를 학습 순서의 기준으로 사용합니다. 유료 강의 자료나 코드를 복제하지 않고, 공식 문서와 공개 자료를 활용해 독립적으로 구현합니다.

## 학습 목표

- 대규모 데이터와 트래픽이 기존 CRUD 시스템에 만드는 병목을 직접 재현한다.
- 요구사항과 장애 조건을 기준으로 기술을 선택하고, 선택 근거를 설명할 수 있다.
- Scale-up과 Scale-out, Monolithic Architecture와 Microservice Architecture의 차이와 비용을 이해한다.
- MySQL의 인덱스, 실행 계획, 트랜잭션, 락, 복제와 샤딩을 코드와 실험으로 검증한다.
- Snowflake 기반 분산 ID와 다양한 Primary Key 생성 전략을 비교한다.
- 계층형 댓글을 관계형 데이터베이스에 모델링하고 조회·정렬·삭제 전략을 비교한다.
- 높은 쓰기 트래픽에서 동시성과 데이터 일관성을 보장하는 방법을 구현한다.
- Redis를 활용한 원자적 연산, 분산 상태 관리와 캐시 전략의 장단점을 이해한다.
- Kafka 기반 이벤트 스트리밍과 비동기 처리를 구현하고 중복·순서·유실 문제를 다룬다.
- Transactional Outbox 등 트랜잭셔널 메시징 기법으로 DB 변경과 이벤트 발행 사이의 간극을 줄인다.
- CQRS와 조회 모델, 비정규화, 캐시와 Request Collapsing을 이용해 조회 경로를 최적화한다.
- 테스트 통과 여부뿐 아니라 실행 계획, 처리량, 지연 시간과 장애 복구 결과를 근거로 개선 효과를 판단한다.

## 학습 운영 원칙

1. 공개 강의의 섹션과 강의 순서를 그대로 따른다.
2. 각 주제는 `문제와 요구사항 → 기술 설명 → 대안과 트레이드오프 → 구현 → 테스트 → 과부하·장애 실험 → 결과 해석` 순서로 진행한다.
3. 기본 구현 방식은 완성 코드와 구체적인 코드 설명이다. 직접 구현을 연습할 때만 힌트 방식으로 전환한다.
4. 코드는 클래스 책임, 요청 흐름, 트랜잭션 경계, 실패 조건과 테스트 목적까지 설명할 수 있어야 한다.
5. 학습 프로젝트이므로 대량 데이터, 동시 요청, 장애와 데이터 불일치를 의도적으로 만든다.
6. 현재 섹션을 깊게 이해하는 데 필요한 보충 기술과 실험은 적극적으로 추가한다.
7. 강의 흐름과 무관한 기능 확장이나 기술 스택 추가는 전체 로드맵을 완료한 뒤 검토한다.
8. 블로그 글이나 포트폴리오 문서 작성은 완료 조건에 포함하지 않는다.
9. 별도의 노션 기록 여부와 내용은 학습자가 직접 판단한다.

## 기술 스택

### 현재 프로젝트

- Java 21
- Spring Boot 4.1.0
- Gradle 9.5.1 / Groovy DSL
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- Bean Validation
- Flyway
- MySQL
- Spring Boot Actuator
- Testcontainers
- JUnit 5 / AssertJ
- Lombok

### 로드맵에서 추가

- Docker / Docker Compose
- Redis
- Apache Kafka
- 다중 DataSource와 논리적 Database Sharding
- Database Replication 실험
- Snowflake ID
- Transactional Outbox
- CQRS와 Read Model
- Cache Aside와 Request Collapsing

강의의 기준 환경은 Spring Boot 3.3.2이지만, 이 저장소는 프로젝트 생성 시점의 Spring Initializr 안정 버전인 Spring Boot 4.1.0을 사용합니다. 강의와 프레임워크 버전 차이가 발생하면 Spring Boot 4와 Spring Security 7 기준으로 구현하고 차이를 기록합니다.

## 전체 로드맵

공개 강의의 8개 섹션 순서를 그대로 따르며, 각 섹션에 필요한 실무형 구현과 검증 실험을 추가합니다.

### Section 1. 들어가며

#### 강의 흐름

1. 강의 소개
2. 대규모 시스템 서버 인프라 기초
3. 강의자료 다운로드
4. 시스템 아키텍처 - Monolithic Architecture
5. 시스템 아키텍처 - Microservice Architecture
6. Docker
7. Spring Boot 프로젝트 세팅 1
8. Spring Boot 프로젝트 세팅 2

#### 학습 및 실습

- 대규모 시스템의 기준과 주요 병목 지점
- Scale-up과 Scale-out
- Load Balancer와 Stateless 애플리케이션
- Monolithic Architecture와 Microservice Architecture 비교
- Docker 이미지, 컨테이너, 네트워크와 볼륨
- MySQL 기반 로컬 개발 환경 구성
- Spring Boot 4 프로젝트와 패키지 구조 설정
- JPA, Security, Validation, Flyway, Actuator와 Testcontainers 기본 구성
- 여러 애플리케이션 인스턴스를 실행할 수 있는 기반 준비

#### 완료 조건

- 서버 확장 방식과 상태 공유 문제를 설명할 수 있다.
- 모놀리식과 마이크로서비스 구조의 장점과 비용을 비교할 수 있다.
- Docker Compose로 실습 인프라를 직접 실행하고 종료할 수 있다.
- Spring Boot 애플리케이션과 MySQL 연결을 테스트로 검증한다.

---

### Section 2. 게시글

#### 강의 흐름

- Distributed Relational Database
- MySQL 개발 환경 세팅
- 게시글 CRUD API 설계와 구현
- Snowflake
- 게시글 테스트 데이터 삽입
- 페이지 번호 기반 목록 조회
- 전체 게시글 개수 조회
- 무한 스크롤 기반 목록 조회
- Primary Key 생성 전략

#### 학습 및 실습

- 게시글 API와 데이터 모델 설계
- 인증 사용자를 기반으로 한 작성자 식별과 권한 검증
- DTO, Bean Validation, 예외 처리와 트랜잭션 적용
- Flyway 기반 스키마 및 인덱스 관리
- Snowflake ID 구현과 서버 ID 충돌 실험
- Auto Increment, UUID와 Snowflake 비교
- 게시글 10만·100만 건 이상의 테스트 데이터 생성
- OFFSET 기반 페이지네이션과 커서 기반 조회 비교
- `COUNT(*)` 비용과 페이지 수 계산 전략
- Clustered Index, Secondary Index와 Covering Index
- `EXPLAIN ANALYZE`를 이용한 실행 계획 분석
- 복합 인덱스 순서에 따른 조회 성능 비교
- 다중 MySQL 인스턴스를 이용한 논리적 샤딩
- 샤드 키, 데이터 편중, Hot Shard와 전체 목록 병합 실험

#### 완료 조건

- 분산 환경에서 ID 생성 전략이 필요한 이유를 설명할 수 있다.
- OFFSET과 커서 페이지네이션의 성능·정합성 차이를 실험 결과로 설명할 수 있다.
- 쿼리 조건과 정렬에 맞는 인덱스를 직접 설계할 수 있다.
- 샤딩이 해결하는 문제와 새로 만드는 문제를 설명할 수 있다.

---

### Section 3. 댓글

#### 강의 흐름

- 최대 2-depth 댓글 테이블 설계
- 최대 2-depth 댓글 CUD와 목록 API
- 테스트 데이터 생성과 조회 검증
- 무한-depth 댓글 테이블과 구현 설계
- 무한-depth 댓글 CUD와 목록 API

#### 학습 및 실습

- 자기 참조 테이블과 계층형 데이터 모델링
- 최대 2-depth 요구사항과 무한-depth 요구사항 비교
- Adjacency List와 Path Enumeration
- 경로 기반 정렬과 하위 트리 조회
- 데이터베이스 Collation이 경로 정렬에 미치는 영향
- 부모 댓글 삭제와 자식 댓글 유지 정책
- 논리 삭제와 물리 삭제 비교
- 깊은 댓글과 편중된 댓글 트리에 대한 조회 성능 실험
- Closure Table 등 대안 설계 비교

#### 완료 조건

- 댓글 깊이 요구사항에 따라 다른 모델을 선택할 수 있다.
- 계층형 데이터의 읽기·쓰기 비용과 삭제 전략을 설명할 수 있다.
- 경로 기반 댓글 정렬과 조회를 구현하고 테스트할 수 있다.

---

### Section 4. 좋아요

#### 학습 및 실습

- 좋아요 생성·취소와 중복 요청 처리
- `(article_id, member_id)` Unique Constraint
- 조회 후 증가 방식에서 발생하는 Lost Update 재현
- 원자적 UPDATE
- Record Lock과 트랜잭션 격리
- Optimistic Lock과 Pessimistic Lock
- 멱등성과 재시도 처리
- 다중 애플리케이션 인스턴스에서 동시성 테스트
- Redis Distributed Lock과 데이터베이스 방식 비교

#### 완료 조건

- 애플리케이션 검증과 DB 제약 조건의 역할을 구분할 수 있다.
- Lost Update를 재현하고 여러 해결 방법의 처리량과 복잡성을 비교할 수 있다.
- 단일 서버 락이 분산 환경을 보호하지 못하는 이유를 설명할 수 있다.

---

### Section 5. 조회수

#### 학습 및 실습

- MySQL 직접 증가 방식과 Hot Row 문제
- 동일 게시글에 집중되는 높은 쓰기 트래픽 재현
- Redis 자료구조와 `INCR` 원자 연산
- 사용자·쿠키 기반 중복 조회 방지 정책
- Write-through와 Write-back 비교
- Redis 조회수를 MySQL로 배치 반영
- 동기화 중 애플리케이션 또는 Redis 장애 실험
- 정확한 수치와 근사 수치에 필요한 일관성 수준 정의

#### 완료 조건

- 조회마다 DB UPDATE를 수행할 때 발생하는 병목을 설명할 수 있다.
- Redis 도입으로 얻는 성능과 감수해야 하는 데이터 유실 위험을 설명할 수 있다.
- 조회수 도메인에 맞는 정합성 정책과 복구 전략을 설계할 수 있다.

---

### Section 6. 인기글

#### 학습 및 실습

- 인기 점수와 기간별 인기글 요구사항 설계
- 동기 처리와 비동기 처리 비교
- Event-Driven Architecture
- Kafka Topic, Partition, Offset와 Consumer Group
- 이벤트 순서, 중복 소비와 At-least-once 처리
- 게시글·좋아요·댓글·조회 이벤트 발행
- 이벤트 기반 인기글 집계
- 멱등 Consumer와 재처리 전략
- DB 커밋과 Kafka 발행 사이의 원자성 문제 재현
- Distributed Transaction과 Two-Phase Commit 비교
- Transactional Outbox 구현
- Outbox Relay, CDC와 Transaction Log Tailing 비교
- Producer·Consumer·Kafka 장애와 이벤트 적체 실험

#### 완료 조건

- 이벤트 전달 보장 수준과 순서 보장 범위를 설명할 수 있다.
- 이벤트 유실과 이벤트 중복을 별개의 문제로 다룰 수 있다.
- Transactional Outbox가 해결하는 문제와 한계를 코드로 설명할 수 있다.

---

### Section 7. 게시글 조회 최적화

#### 학습 및 실습

- 게시글 상세 조회에 필요한 여러 도메인 데이터 조합
- 서비스 간 동기 호출이 만드는 지연과 장애 전파
- CQRS와 Read Model
- 비정규화된 조회 전용 데이터 구성
- 이벤트 기반 조회 모델 갱신
- 원본 데이터와 조회 모델 사이의 Eventual Consistency
- Cache Aside
- TTL과 캐시 무효화
- Cache Stampede와 Hot Key
- Request Collapsing
- Redis 장애 시 DB Fallback
- 캐시와 DB 데이터 불일치, 갱신 지연과 복구 실험

#### 완료 조건

- CQRS를 단순한 코드 계층 분리가 아닌 데이터 모델 분리 관점에서 설명할 수 있다.
- 조회 모델의 성능 이점과 갱신 복잡성을 비교할 수 있다.
- 캐시 장애가 발생해도 핵심 조회 기능이 동작하도록 설계할 수 있다.

---

### Section 8. 마치며

#### 학습 및 실습

- 전체 요청과 데이터 흐름 복기
- 기술별 도입 이유와 트레이드오프 정리
- 기능·성능·정합성·장애 대응 검증 결과 확인
- 남아 있는 한계와 운영 확장 범위 정의

최종 시스템 흐름은 다음과 같은 구조를 목표로 합니다.

```text
HTTP 요청
→ Spring Boot 애플리케이션
→ MySQL 상태 변경과 Outbox 저장
→ Kafka 이벤트 발행
→ Consumer 처리
→ 인기글·조회 모델 갱신
→ Redis 캐시 또는 카운터 반영
→ 사용자 조회 응답
```

#### 전체 완료 조건

- 각 기술을 사용하는 이유를 문제 상황과 연결해 설명할 수 있다.
- 구현 코드의 요청 흐름과 트랜잭션 경계를 설명할 수 있다.
- 테스트와 측정 결과로 기능 정확성 및 성능 개선을 증명할 수 있다.
- 장애가 발생했을 때 유실·중복·지연·복구 범위를 설명할 수 있다.
- 선택하지 않은 대안과 현재 설계의 한계를 설명할 수 있다.

## 실험 원칙

- 처음부터 최적화된 구조를 적용하지 않고 단순 구현의 한계를 먼저 재현한다.
- 성능 개선 전후에 동일한 데이터와 조건을 사용한다.
- 평균 응답 시간만 보지 않고 처리량, 지연 분포, 읽은 레코드 수와 오류율을 함께 확인한다.
- 동시성 테스트에서는 기대한 최종 값과 실제 값을 비교한다.
- 장애 테스트에서는 데이터 유실, 중복, 지연과 재처리 가능 여부를 확인한다.
- Redis, Kafka와 샤딩은 기술 스택을 늘리기 위한 장식이 아니라 문제 해결 수단으로 사용한다.

## 현재 진행 상태

- [x] Spring Initializr 기반 프로젝트 생성
- [x] Java 21 / Spring Boot 4.1.0 설정
- [x] JPA, Security, Validation, Flyway, Actuator와 Testcontainers 의존성 구성
- [x] 기본 브랜치를 `main`으로 통합
- [ ] Section 1. Docker와 로컬 인프라 구성
- [ ] Section 1. Spring Boot 프로젝트 기본 설정 및 실행 검증
- [ ] Section 2. 게시글
- [ ] Section 3. 댓글
- [ ] Section 4. 좋아요
- [ ] Section 5. 조회수
- [ ] Section 6. 인기글
- [ ] Section 7. 게시글 조회 최적화
- [ ] Section 8. 전체 검증

## 로드맵 완료 후 확장 가능 범위

본 로드맵을 완료한 뒤에는 별도 운영 단계로 확장할 수 있습니다.

- 부하 테스트 시나리오 고도화
- Prometheus와 Grafana 기반 관측성
- 구조화 로그와 분산 추적
- 애플리케이션 및 인프라 장애 대응
- 백업·복구와 데이터 정합성 점검
- CI/CD와 배포 자동화
- 클라우드 배포와 수평 확장
- Rate Limiting, Circuit Breaker와 운영 보안
