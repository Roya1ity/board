---
title: 알려진 문제와 결정 필요 사항
tags: [board, risks, tech-debt, decisions]
status: living-document
---

# 알려진 문제와 결정 필요 사항

우선순위는 P0(핵심 기능/보안 차단), P1(계약 신뢰성), P2(유지보수/일관성)다. 이 목록은 코드 분석 결과이며 아직 수정 작업을 수행했다는 뜻은 아니다.

## P0 — 기능을 깨뜨리는 문제

- **반응이 저장되지 않음**: `ReactionService.postReacttion`은 새/기존 엔티티의 type만 바꾸고 repository `save` 또는 삭제를 호출하지 않는다. 트랜잭션도 없다. 집계도 저장 전 조회한다.
- **반응 취소 모델 충돌**: 동일 타입 재요청 시 type을 null로 바꾸지만 `PostReaction.type`은 `nullable=false`다. 취소는 행 삭제로 정의하는 편이 안전하다.
- **회원가입 권한 상승**: `SignupRequest.role`을 클라이언트가 보내고 `admin`이면 ADMIN 계정이 생성된다. 공개 회원가입은 서버가 USER를 강제해야 한다.
- **비밀정보 하드코딩/로그**: datasource password와 JWT secret이 설정 파일에 있고 JWT provider가 secret을 DEBUG 로그로 출력한다.
- **예외 HTTP status 왜곡**: `GlobalExceptionHandler`가 모든 `BusinessException`을 409로 응답해 `ErrorCode`의 400/401/403/404/500 의미를 무시한다.

## P1 — API와 데이터 신뢰성

- 반응 URL과 메서드에 `reation`, `postReacttion` 오타가 있다. 목표 경로 `/api/reaction`으로 변경 시 호환/마이그레이션 전략 필요.
- 댓글 반응 엔티티/repository는 있으나 서비스·API·응답 통합이 없다.
- `buildPostReactionResponse(postId, null)`이 null userId 파생 쿼리에 의존한다. 명시적으로 비회원 분기를 두는 편이 안전하다.
- 게시글 상세이 `findById`를 사용해 LAZY 관계/이미지 로딩과 N+1 또는 세션 의존 위험이 있다. 이미 정의된 `findDetailById`는 사용하지 않는다.
- 게시글/게시판 삭제가 반응, 알림, 실제 이미지 파일까지 정리한다고 보장되지 않는다.
- 존재하지 않는 게시글 삭제 시 명시적 조회 없이 `deleteById`를 호출하고 댓글부터 삭제한다.
- 답글 `parentId`가 요청의 `postId`와 같은 게시글 소속인지 검증하지 않는다.
- 답글 깊이 제한이 없어 임의 깊이가 가능하지만 응답은 루트의 direct children만 재귀 변환하며 손자 구조가 누락될 수 있다.
- 알림 조회 실패에 `COMMENT_NOT_FOUND`를 사용한다.
- 별도 Kakao callback은 state cookie 존재만 확인하고 query state와 동등 비교하지 않으며, 만들어 둔 refresh cookie를 응답 header에 넣지 않는다.
- access token prefix가 로그인(`Bearer ` 포함)과 재발급(미포함)에서 다르다.

## P2 — 일관성과 유지보수

- 테이블명 `commnent`, 설정 키 `refressh-token-validity-seconds`, 응답 필드 `createAt`, reaction 관련 철자 오류.
- `UserProfile` 테이블명이 `User`, 인증 계정 테이블명이 `Auth`라 도메인 이름과 DB 이름이 반대로 읽힌다.
- Board 이름 중복은 애플리케이션에서만 검사하고 DB unique constraint가 없다.
- 게시판 생성은 ADMIN 검사와 서비스 검증이 주석 처리되어 로그인 사용자 누구나 가능하다.
- `PostDTO` 변환 overload마다 반응/권한 필드 채움이 다르다.
- 게시글 조회수는 같은 방문자의 반복 요청도 모두 센다.
- 댓글 삭제 응답은 200 empty, 다른 삭제는 JSON `IngestResult`로 일관되지 않다.
- 미사용 DTO/import/주석 처리 코드와 `System.out.println` 디버그 출력이 남아 있다.
- 오류 메시지 소스 일부는 인코딩이 깨져 보인다.
- 자동화 테스트는 컨텍스트 로드 1개뿐이다.

## 프론트엔드 착수 전 결정

| ID | 질문 | 권고 기본값 | 상태 |
|---|---|---|---|
| D-01 | 공식 OAuth 흐름은 무엇인가? | Spring Security OAuth2 하나로 통합 | 미정 |
| D-02 | access token 저장 위치는? | 메모리 + refresh cookie 복구 | 미정 |
| D-03 | 반응 취소는 null 상태인가 행 삭제인가? | 행 삭제 | 미정 |
| D-04 | 댓글 답글 최대 깊이는? | UI/API 모두 1단계 | 미정 |
| D-05 | 게시글 이미지 수정 범위는? | 추가/삭제/순서 변경 API 제공 | 미정 |
| D-06 | 게시판 생성 권한은? | ADMIN 전용 | 미정 |
| D-07 | 날짜 API timezone/형식은? | ISO-8601 offset 포함 | 미정 |
| D-08 | 페이지 번호 UI는 0/1 중 무엇인가? | API 0-based, UI 1-based | 미정 |
| D-09 | 조회수 중복 기준은? | 세션/기간 기반 중복 방지 | 미정 |
| D-10 | 새 프론트의 배포 origin은? | 동일 origin 우선 | 미정 |

## 권고 수정 순서

1. 비밀정보/ADMIN 회원가입/예외 status 문제를 먼저 해결한다.
2. 반응 토글을 트랜잭션 + create/update/delete로 고치고 통합 테스트를 추가한다.
3. 삭제 연쇄와 댓글 parent-post 불변조건을 DB/서비스 양쪽에서 보강한다.
4. API 명명과 응답 일관성을 버전 전략과 함께 정리한다.
5. 결정표를 확정한 뒤 [[06-frontend-requirements]]를 구현 기준으로 고정한다.

