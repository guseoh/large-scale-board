# Large-Board

**Pure Java → Spring Boot → 대규모 단일 시스템 → 분산 시스템**으로 발전시키는 Java 백엔드 학습 프로젝트.

목표는 게시판 완성이나 기술 스택 나열이 아니다. 같은 기능을 직접 구현하고 데이터·트래픽·장애 조건을 바꾸면서 **시스템의 동작 원리, 문제 원인, 설계 선택과 Trade-off를 스스로 설명하고 판단하는 역량**을 기른다. 순수 학습용이므로 포트폴리오 제작·배포·블로그 작성은 완료 조건이 아니다.

## 학습 순서

```text
Pure Java J01~J05 (구현 복습)
→ Project Design Baseline (Spring API·DB 구현 전)
→ Spring S01~S08 (Phase 0·1의 기본 구현)
→ Part 1: Phase 0~7 (대규모 단일 시스템)
→ Part 2: Phase 8~13 (분산 시스템)
```

- **Pure Java·Spring:** 파일 경로와 완성 코드를 제공받아 IntelliJ에서 직접 작성·실행한다. 설명은 기본적으로 핵심 책임과 동작 흐름만 간결하게 다룬다.
- **긴 이론 설명:** 기본 진행에 포함하지 않는다. 사용자가 명시적으로 요청할 때만 심화하며, 필요한 경우 별도 채팅에서 진행한다.
- **퀴즈·확인 문제:** 필수 학습 단계에 넣지 않는다. 별도의 퀴즈나 과제를 자동으로 부여하지 않는다.
- **대규모·분산 단계:** 단순 구조의 문제를 재현·측정하고, ChatGPT는 해결 후보와 Trade-off를 제시하며 학습자가 설계를 판단한다.

## 선행 구현 복습

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

Pure Java 코드는 기존 저장소의 `io.github.guseoh.board.practice` 패키지에서 시작한다. 별도 Todo/게시판 프로젝트는 만들지 않는다. Spring 복습에서 구현한 부분은 Phase 0·1에서 다시 만들지 않지만 **Phase 전체 완료는 상세 로드맵의 실험·검증 기준**으로 판단한다.

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

## 현재 학습 상태

2026-10-22 확인 기준으로 최소 Spring 실행 환경과 MySQL 연결은 **과거 실행 검증 기록**이 있으며, 이번 문서 정리에서 재실행하지 않았다. 원격에는 게시판 비즈니스 코드가 없다.

- [ ] Pure Java J01~J05
- [ ] Project Design Baseline
- [ ] Phase 0 미완료 관측·성능 기준선
- [ ] Spring S01~S08 및 Phase 1 이후 실험

**다음 학습:** J01 — Article 객체부터 직접 구현한다.

## 문서

- [상세 로드맵](docs/ROADMAP.md): 기존 Phase 0~13의 상세 범위·완료 기준
- [학습 인수인계](docs/PROJECT_HANDOFF.md): 코드 제공 방식·설계 학습·Git 운영
- [ChatGPT 프로젝트 지침 참고본](docs/CHATGPT_PROJECT_INSTRUCTIONS.md): 프로젝트 설정에 적용할 지침 텍스트
- [GitHub Wiki](https://github.com/guseoh/large-scale-board/wiki): 장기 보존할 설계 변경·실험 기록
