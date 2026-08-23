# AGENTS.md

## Project

- Java 21, Spring Boot, Spring Security, JPA, MySQL, Flyway 기반 학습 프로젝트다.
- 기본 CRUD는 이후 대규모 트래픽, 성능, 동시성, 분산 시스템 학습을 위한 기준 구현이다.
- 기존 코드 구조와 설정을 우선한다.

## Development

- 필요한 범위만 구현하고 과도한 추상화와 공통화를 하지 않는다.
- Flyway로 스키마를 변경하고 적용된 마이그레이션은 수정하지 않는다.
- JPA는 `ddl-auto=validate`를 유지한다.
- Entity와 API DTO를 분리한다.
- 트랜잭션 경계는 Service 계층에 둔다.
- 요청 값은 Bean Validation으로 검증한다.
- 인증이 필요한 기능은 클라이언트가 전달한 회원 ID가 아니라 인증 사용자 정보를 사용한다.
- 비밀번호 원문을 저장하지 않는다.
- 불필요한 주석과 설명성 코드를 추가하지 않는다.

## Baseline

- 기본 CRUD에는 필요한 PK, FK, UNIQUE, NOT NULL 등의 제약만 적용한다.
- 성능 문제를 재현하고 측정하기 전에 선행 최적화하지 않는다.
- 명시적으로 요청하기 전에는 Redis, Kafka, Cache, CQRS, Outbox, Sharding, Replication, Distributed Lock 등 심화 기술을 추가하지 않는다.

## Test

- 변경 후 `./gradlew test`를 실행한다.
- 변경으로 발생한 테스트 실패는 수정한다.

## Git

- 명시적으로 요청하지 않으면 commit, push, branch 생성은 하지 않는다.
- 관련 없는 기존 코드를 변경하지 않는다.
