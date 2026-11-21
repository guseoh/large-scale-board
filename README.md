# Large-Board

**Spring REST API 기본기 → 작은 게시판 CRUD → 실제 문제 측정 → 대용량·분산 시스템 실험**으로 발전시키는 Java/Spring 학습 저장소입니다.

이 저장소의 목적은 처음부터 큰 시스템을 완성하는 것이 아닙니다. 먼저 HTTP 요청을 받고, 검증하고, 데이터를 저장하고, 응답하는 **가장 기본적인 흐름을 직접 구현**합니다. 이후 실제 데이터·트래픽·장애 조건에서 문제가 드러날 때만 더 복잡한 설계를 비교·도입합니다. 포트폴리오·배포·문서량은 완료 조건이 아닙니다.

## 현재 최우선 원칙 (2026-11-21 확정)

1. **지금은 Spring REST API를 먼저 학습합니다.** Article CRUD, 요청/응답 DTO, Validation, JPA 기초, 트랜잭션, 페이징, 최소 예외 처리, 테스트를 완성하기 전에는 분산·고성능 설계를 추가하지 않습니다.
2. **단순한 기본 구현을 먼저 사용합니다.** 초반 Article ID는 일반적인 JPA ID 전략부터 경험하고, 공통 `BaseEntity`·Snowflake·복잡한 오류 계약을 자동 적용하지 않습니다. 기존 클래스는 삭제하지 않고 비교 실험 자산으로 유지합니다.
3. **보호해야 할 위험은 처음부터 보호합니다.** 입력 유효성, 인증·인가가 필요한 기능, DB 무결성 제약, 트랜잭션 경계, 비밀정보 노출 방지는 생략하지 않습니다. 다만 불필요한 사전 방어·범용 추상화·광범위 예외 분류를 만들지 않습니다.
4. **실험은 문제를 관찰한 뒤 시작합니다.** 기존 Snowflake, `datagen`, Prometheus/Grafana, 장애 훈련 및 이미 확인된 측정 결과는 보존합니다. 새 기능을 시작할 때 이들을 의무 선행 조건으로 만들지 않습니다.
5. **학습의 완료 기준은 실행 가능한 작은 기능입니다.** 실제 요청 → Controller → Service → Repository → DB → 응답 흐름과 실패 사례를 검증합니다. 전체 아키텍처·ERD·NFR을 초반에 완성하도록 요구하지 않습니다.

작업 시 구체적인 기준은 [루트 AGENTS.md](AGENTS.md), [학습 인수인계](docs/PROJECT_HANDOFF.md)를 따릅니다. 이 규칙은 **앞으로의 진행 순서**를 바꾸는 것이며, 기존 로드맵 Phase 0~13, 완료된 실험과 ADR의 이력을 삭제하거나 무효화하지 않습니다.

## 현행 학습 순서

```text
현재: Spring REST API 기초 S01~S08 (Article 최소 CRUD와 동작 확인)
→ 기본 게시판 완성 및 필요한 테스트
→ 필요할 때 Pure Java J01~J05 문법·객체 복습 / 작은 설계 보완
→ 기존 Phase 0~7의 대용량 단일 시스템 실험
→ 기존 Phase 8~13의 분산 시스템 실험
```

- **직접 구현:** 사용자가 `src/main`에 애플리케이션 코드를 작성하고, AI는 요청된 코드·짧은 설명을 제시하며 테스트·측정 도구를 지원합니다. 사용자 요청 없이 AI가 `src/main`을 바꾸지 않습니다.
- **학습 진행:** 긴 이론 글·자동 퀴즈·문서 제작을 필수로 하지 않습니다. 단순 구현 → 문제 재현 → 선택지 비교 → 개선 → 재검증의 순서를 지킵니다.
- **범위 제한:** Spring Security, JWT, Redis, Kafka, 샤딩, 분산 락, 복잡한 아키텍처 규칙과 관측 시스템을 기초 CRUD의 진입 조건으로 삼지 않습니다. 필요성·실험 목적이 있는 경우에만 개별적으로 다룹니다.

## REST API 기본 구현과 선택 복습

아래 S01~S08을 현재의 우선 진행 단위로 사용합니다. J01~J05는 Java 객체 문법이 막힐 때 **선택적으로 복습**하며, 선행 통과 조건으로 강제하지 않습니다.


| Slice | 구현할 내용 |
| --- | --- |
| J01 | Article 객체와 최소 비즈니스 규칙 |
| J02 | Map 기반 Article 저장소 |
| J03 | ArticleService와 CRUD |
| J04 | 예외 처리와 경계 조건 |
| J05 | 실행·JUnit·AssertJ 테스트 |
| S01 | 기존 Spring Boot·MySQL 환경 확인 |
| S02 | Controller·Service와 HTTP API |
| S03 | 요청·응답 DTO와 기본 Validation |
| S04 | JPA Entity·Repository·MySQL·트랜잭션 |
| S05 | CRUD·목록 조회·페이징 기초 |
| S06 | 필요한 시점에 Member·세션 기반 인증과 인가 |
| S07 | 전역 예외 처리와 오류 응답 |
| S08 | 기능 테스트·SQL·Actuator 검증 |

Pure Java 복습을 선택한 경우에만 이 저장소의 `io.github.guseoh.board.practice` 패키지를 사용합니다. **Article API 구현은 기존 `Member` 인증·Snowflake 설계와 독립적으로 시작**할 수 있습니다. 기존 Phase의 측정·실험 완료 기준은 변경하지 않으며, 이미 검증된 실험을 반복할 의무도 없습니다.

## 기존 Phase 0~13

**각 Phase의 세부 학습 범위·실험·완료 기준은 [docs/ROADMAP.md](docs/ROADMAP.md)를 따른다.** 기존 내용을 삭제하거나 단축하지 않고 별도 문서로 옮겼다.

| Part | Phase | 영역 |
| --- | --- | --- |
| 대규모 단일 시스템 | 0 | 시스템 기반과 개발 환경 |
| | 1 | 게시글과 분산 데이터베이스 |
| | 2 | 계층형 댓글 |
| | 3 | 좋아요와 동시성 |
| | 4 | 조회수와 높은 쓰기 트래픽 |
| | 5 | 인기글과 이벤트 기반 아키텍처 |
| | 6 | 게시글 조회 최적화 |
| | 7 | 전체 시스템 통합과 검증 |
| 분산 시스템 | 8 | Modular Monolith와 서비스 경계 |
| | 9 | Microservice 분리 |
| | 10 | 서비스 간 동기 통신과 장애 전파 |
| | 11 | 분산 데이터 일관성 |
| | 12 | Multi-instance와 Load Balancing |
| | 13 | Distributed Observability와 장애 실험 |

## 현재 실행 환경

Java 21 · Spring Boot 4.1.0 · Gradle 9.5.1 · Spring MVC · Spring Data JPA/Hibernate · MySQL 8.4 · Docker Compose · Validation · Actuator/Micrometer · JUnit/AssertJ/Testcontainers.

Windows PowerShell / IntelliJ를 기준으로 한다. 기존 포트는 Spring Boot `8081`, MySQL `3310`이다.

```powershell
docker compose up -d
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

전체 테스트에는 Docker가 필요하다. Pure Java의 독립 JUnit 테스트는 MySQL 없이 실행할 수 있다. 현재 `spring.jpa.hibernate.ddl-auto=create`는 시작 시 스키마·데이터를 초기화할 수 있으므로 데이터 보존 실험 전에 검토한다.

## 현재 학습 상태 (원격 `main` 확인: 2026-11-21)

- **확인:** `main`에는 Spring Boot/MySQL 환경, `Member` 기본 모델, Snowflake/직접 할당 ID, 예외 처리, 요청 추적, `/ping`·`/ping/db`, 성능 실험·테스트 코드가 있습니다. 2026-11-19의 `main` CI는 성공했습니다.
- **별도 이력:** Phase 0의 DB 적재·장애 훈련과 Phase 1 일부(1-A 및 트랜잭션 실험)는 기록과 검증 근거가 있습니다. 이는 **REST API 기본기 완료**를 뜻하지 않습니다.
- **아직 완료라고 할 수 없음:** Article CRUD 및 Spring REST API S01~S08 전체. `phase/1b-signup` 등 진행 중인 브랜치는 `main`과 구분하고, 미병합 작업을 완료로 간주하지 않습니다.
- **다음 학습:** 기존 환경(S01)을 짧게 확인한 뒤 **S02 Article Controller → 기본 HTTP API**부터 진행합니다. 그다음 DTO/Validation → JPA 저장·조회 → 수정·삭제/트랜잭션 → 페이징 → 필요한 예외/테스트 순으로 확장합니다.

코드의 런타임·전체 테스트는 이번 문서 변경에서 다시 실행하지 않았습니다. 실제 진도는 해당 기능 구현과 테스트가 확인된 때에만 갱신합니다.

## 문서

- [상세 로드맵](docs/ROADMAP.md): 기존 Phase 0~13의 상세 범위·완료 기준
- [학습 인수인계](docs/PROJECT_HANDOFF.md): 코드 제공 방식·설계 학습·Git 운영
- [ChatGPT 프로젝트 지침 참고본](docs/CHATGPT_PROJECT_INSTRUCTIONS.md): 프로젝트 설정에 적용할 지침 텍스트
- [GitHub Wiki](https://github.com/guseoh/large-scale-board/wiki): 장기 보존할 설계 변경·실험 기록
