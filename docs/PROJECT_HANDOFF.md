# Large-Board 학습 프로젝트 인수인계

> 기준일: 2026-10-22. 이 문서는 **진행 방법**을 정의한다. 프로젝트 진입점은 [README](../README.md), Phase별 상세 기준은 [ROADMAP](ROADMAP.md)이다.

## 목표와 학습 단계

개인 학습용 Java 백엔드 프로젝트. 코드를 직접 다루는 감각을 복습한 뒤, 단순 시스템이 성능·정합성·장애 문제를 겪으며 복잡해지는 이유를 실제 실험으로 이해한다. 포트폴리오·문서 제작이나 빠른 완성은 목표가 아니다.

```text
Pure Java J01~J05
→ Project Design Baseline
→ Spring S01~S08 (Phase 0·1 기본 구현과 연결)
→ 기존 Phase 0~7
→ 기존 Phase 8~13
```

기존 Phase 0~13의 상세 학습 항목과 완료 기준은 그대로 유지한다. Spring 복습에서 이미 구현한 CRUD 등을 반복하지 않는다. Phase 전체 완료는 실험과 검증까지 수행한 경우에만 인정한다.

## 초기 학습 방식 — 완성 코드 중심

ChatGPT의 기본 답변 순서:

```text
현재 Slice 목표·범위
→ 정확한 파일 경로와 실행 가능한 파일별 완성 코드
→ 실행 명령과 정상 예상 결과
→ 핵심 객체/요청/데이터 흐름의 짧은 설명
→ 다음 Slice
```

- 사용자는 IntelliJ에서 코드를 직접 입력하고 실행한다. 기본 구현을 Codex나 Work에 맡기지 않는다.
- **긴 이론 강의는 기본 제공하지 않는다. 사용자가 필요하다고 요청할 때만 상세히 설명한다.** 심화 질문은 별도의 채팅 세션에서 진행할 수 있다.
- **필수 퀴즈·이해도 확인 문제·별도 과제는 완전히 제외**한다. 사용자가 요청하지 않은 퀴즈나 학습 점검을 제안하지 않는다.
- 매번 Java 기본 문법과 이미 아는 Spring 애너테이션을 설명하지 않는다. 시스템 동작에 중요한 부분도 기본적으로 핵심 흐름에 필요한 만큼만 짚는다.
- 오류가 발생하면 실제 로그와 코드로 해결한다. 아직 실행하지 않은 기능은 완료로 표시하지 않는다.
- 각 Slice는 지나치게 비대하게 만들지 않으며, 현재 필요하지 않은 패턴과 기술을 선도입하지 않는다.

## 심화 단계 — 사용자 설계 주도

대규모·분산 시스템 단계는 단순 구조와 측정 기준선을 먼저 확보한다.

```text
현재 구조 → 문제 재현 → 기준선 측정 → 원인 후보 검증
→ 복수의 설계 선택지·Trade-off 제시 → 사용자 선택
→ 직접 구현 → 동일 조건 재측정 → 장애 실험 → 결과 해석
```

이 단계에서 ChatGPT는 해결책의 장단점, 정확성·성능·운영·복잡성·장애 비용을 비교하지만 정답 아키텍처를 먼저 고정하지 않는다. 학습자가 문제를 생각하고 설계를 선택한다. 코드나 상세 해결책은 **사용자가 요청할 때** 제공할 수 있다. 측정 없는 성능 단정과 부하·장애 실험의 허위 완료 판정을 피한다.

## 선행 Slice

| Slice | 구현 |
| --- | --- |
| J01 | Article 객체, 생성자, 상태, 캡슐화 |
| J02 | Map 기반 Article 저장소 |
| J03 | Service와 CRUD |
| J04 | 비즈니스 규칙·예외·경계 조건 |
| J05 | 실행 및 JUnit/AssertJ 테스트 |
| S01 | 기존 Spring Boot / MySQL / 테스트 환경 확인 |
| S02 | Article Controller·Service / REST API |
| S03 | Request·Response DTO / 기본 Validation |
| S04 | JPA·Repository·MySQL / 트랜잭션 |
| S05 | CRUD·목록·페이징 기초 |
| S06 | 필요 시 Member·세션 인증·인가 |
| S07 | 전역 예외 처리 / 오류 응답 |
| S08 | 기능 검증·SQL·Actuator |

Pure Java는 현 프로젝트 안의 `io.github.guseoh.board.practice` 패키지에 구현한다. Spring·JPA·MySQL을 이 복습 구간에 사용하지 않는다. 별도 프로젝트 생성은 하지 않는다. Member·Security는 요구사항이 생기는 시점에 설계한다. JWT는 세션보다 자동으로 우월하다고 보지 않는다.

## Project Design Baseline

J01 전에 Article 객체의 최소 상태와 규칙만 결정한다. Spring의 본격적인 API/DB 구현 전에는 다음을 설계한다.

```text
Scope → Actor → Use Case → Business Rule → Domain Map
→ 초기 데이터 모델/ERD → API → 요청·데이터 흐름
→ Application/Database Architecture → NFR·실험 가설
→ 현재 단계 Physical Design
```

전체 도메인은 개념적으로 파악하되, 실제 테이블·API·코드는 현재 Phase가 요구하는 범위에 제한한다. 이후 실험에 따라 설계와 ERD를 수정한다.

## Git·Issue·Wiki 운영

- 기존 저장소·기술 버전·설정 유지. Windows / IntelliJ / PowerShell 사용.
- 기본 작업은 `main` 직접 수행한다. Phase 브랜치와 PR은 필수가 아니며, 대안 실험용 임시 브랜치는 선택 가능하다.
- 평소 코드 구현과 Git 커밋·푸시는 사용자 직접 수행. 사용자가 명시적으로 요청한 문서·Git 작업만 ChatGPT가 대신할 수 있다.
- Issue는 의미 있는 실험·성능 개선·장애·대안 비교에 필요할 때만 사용하며, 단순 CRUD마다 만들지 않는다.
- Wiki에는 중요한 ERD·Architecture 진화, Sharding/Outbox/CQRS/분산 실패 분석을 기록한다. 블로그·포트폴리오 제작은 완료 기준에서 제외한다.
- Docker·DB·네트워크 등 인프라는 필요할 때 구성하고 장애·성능 실험으로 검증한다.

## 새 세션과 진행 상태

새 세션에서는 README, 관련 ROADMAP Phase, 이 인수인계서, 접근 가능한 Wiki를 확인하고 최신 원격 `main`, 브랜치, 열린 PR/Issue와 코드를 검토한다. 과거 대화로 런타임 상태를 추정하지 않는다.

2026-10-22 문서 정리 이전 베이스라인: `main fc3df2f`, 원격 브랜치 `main` 하나, 열린 PR/Issue 없음. 게시판 비즈니스 구현 없음. 사용자에 따르면 로컬 미커밋 변경 없음. Spring Boot/MySQL 실행은 과거 검증이며 현재 실행을 새로 확인한 것은 아니다.

**진행 상태:** J01~J05 미시작; Project Design Baseline 미완료; Phase 0 관측 미완료. 다음은 **J01 Article 모델**이다.

사용자가 정확히 `오늘 학습 종료`라고 입력하면 실제로 검증한 구현·실험·Git 상태, 배운 핵심 흐름과 남은 문제, 다음 Slice를 간결히 기록한다. 실행하지 않은 결과는 완료로 표시하지 않는다.
