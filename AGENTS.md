# 프로젝트 작업 지침

## 프로젝트 개요

- Java 21과 Spring Boot 3.5 기반의 Gradle 프로젝트다.
- 데이터 접근에는 Spring Data JPA를 사용한다.
- 운영 데이터베이스는 MySQL이다.
- 패키지는 기능 단위(`post`, `comment`, `reaction`, `auth` 등)로 구성한다.

## 빌드 및 검증

- Windows에서는 `./gradlew.bat test`, 그 외 환경에서는 `./gradlew test`를 사용한다.
- 변경 후 최소한 수정한 기능의 테스트를 실행한다.
- Repository 쿼리나 엔티티 매핑을 변경한 경우 전체 테스트를 실행해 애플리케이션 컨텍스트와 JPQL 유효성까지 확인한다.
- 작업을 완료하기 전에 `git diff --check`로 공백 오류를 확인한다.

## 코드 변경 원칙

- 요청과 직접 관련된 파일만 수정하고 기존 사용자의 변경 사항은 보존한다.
- Controller는 요청과 응답 처리를 담당하고, 비즈니스 로직은 Service에 둔다.
- Repository에는 데이터 접근 로직만 두며 Service에서 응답 객체를 조립한다.
- 기존 예외 처리 방식인 `BusinessException`과 `ErrorCode`를 우선 사용한다.
- 단순 조회에는 `@Transactional(readOnly = true)`를 사용한다.
- 생성 및 수정 시에는 엔티티의 도메인 메서드를 우선 사용한다.

## JPA 및 조회 성능

- 목록을 순회하며 항목마다 Repository를 호출하는 N+1 쿼리를 만들지 않는다.
- 목록에 필요한 데이터는 ID 목록과 `IN` 조건, fetch join, projection 또는 일괄 집계 쿼리로 조회한다.
- 타입별 개수를 각각 조회하지 말고 `GROUP BY 대상_ID, type` 형태로 한 번에 집계한다.
- 로그인 사용자의 Reaction처럼 개인화된 목록 데이터도 ID 목록으로 한 번에 조회한다.
- 빈 ID 목록으로 `IN ()` 쿼리를 실행하지 않는다.
- 엔티티 전체가 필요하지 않은 집계 결과는 DTO projection을 사용한다.
- 페이지 조회에서 collection fetch join을 사용해 페이지네이션 결과가 깨지지 않도록 주의한다.

## Reaction 도메인

- LIKE와 DISLIKE 집계는 `ReactionType`을 기준으로 처리한다.
- 집계 결과에 특정 타입이 없으면 해당 카운트는 `0`이어야 한다.
- 단건 응답 빌드와 목록용 일괄 응답 빌드를 구분한다.
- 댓글 목록은 루트 댓글과 응답에 포함되는 대댓글의 ID를 모아 Reaction을 일괄 조회한다.
- 비로그인 요청에서는 `myReaction` 조회를 생략하고 `null`을 반환한다.

## 테스트 작성

- Service 테스트에서는 Repository 호출 결과와 주요 상태 변경을 함께 검증한다.
- 일괄 조회 로직은 여러 대상, 누락된 Reaction 타입, 비로그인 사용자, 빈 목록을 포함해 검증한다.
- 쿼리 최적화 작업에서는 결과 값뿐 아니라 Repository 호출 횟수도 검증한다.

## LLM Wiki

- 코드 작성 요청시 LLM Wiki를 참조한다.
- 코드 변경 또는 생성시 LLM Wiki에 해당 내용을 반영시켜 업데이트한다.