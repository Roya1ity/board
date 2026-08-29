---
title: LLM Context
tags: [llm-context, codex, board]
status: current
---

# LLM Context

## Frontend deployment

- `src/main/resources/static` is the canonical browser UI.
- The Nginx frontend mirrors its `index.html`, `styles.css`, and `app.js` files.
- Nginx proxies `/api`, `/images`, and OAuth routes to the backend container.
- GitHub Actions publishes backend and frontend images under the lowercase GHCR namespace `ghcr.io/roya1ity`.
- Production deployment passes `KAKAO_CALLBACK` from the matching GitHub Actions secret into the container `.env` file.
- The frontend container health check requests its local Nginx root page with BusyBox `wget`.

## Database configuration

- The datasource reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` from the environment.
- Local execution defaults to `localhost:3306`, while Compose supplies the `board-mysql` hostname.

## 프로젝트 정체성

Java 21/Spring Boot 3.5.14/MySQL 기반 커뮤니티 백엔드다. 기능 패키지는 auth, board, post, comment, reaction, notification, user이며 JPA 엔티티는 `Global.Entity`에 있다. 정적 vanilla JS 클라이언트가 포함되어 있으나 향후 별도 프론트엔드 요구사항의 출발점일 뿐이다.

## LLM 작업 규칙

1. 현재 코드 사실은 [[03-api-reference]], [[02-domain-model]], [[01-architecture]]에서 확인한다.
2. 제안 요구사항은 [[06-frontend-requirements]]에서 확인하며 현재 구현과 혼동하지 않는다.
3. 변경 전에 반드시 [[07-known-issues-and-decisions]]를 읽는다.
4. 인증 변경은 login/reissue/OAuth/JWT filter/cookie/security config를 한 흐름으로 검토한다.
5. DTO 변경은 기존 `static/app.js` 소비 형태와 page serialization을 함께 확인한다.
6. 삭제 변경은 DB 관계뿐 아니라 로컬 이미지와 값 참조 알림까지 확인한다.
7. 새 endpoint를 만들 때 공개 여부, role/owner 검사, validation, 오류 코드, 테스트를 함께 정의한다.

## 핵심 계약 요약

- 공개 GET: board/post/comment. 나머지는 기본 인증.
- 인증 header: `Authorization: Bearer <JWT>`.
- refresh: HttpOnly `refreshToken` cookie, path `/api/auth`.
- 글 생성: `post` JSON part + 선택 `images` parts, 최대 3개.
- 댓글: `parentId`로 답글, 삭제는 soft delete.
- 글/댓글 수정·삭제: 서버 owner 검사.
- 게시판 수정·삭제: ADMIN. 생성은 현재 인증 사용자 누구나 가능(결함/정책 미정).
- 알림: 댓글 commit 후 별도 트랜잭션으로 생성.
- 반응: 게시글과 댓글 모두 LIKE/DISLIKE를 지원한다. 같은 타입 재요청은 취소, 다른 타입은 전환한다.

## 가장 중요한 위험

- 공개 회원가입에서 ADMIN role 선택 가능.
- 모든 BusinessException이 409로 변환됨.
- secret/password 하드코딩 및 secret 로그 출력.
- API 철자/토큰 prefix/삭제 응답 등 계약 불일치.

## 탐색 순서

```text
docs/README.md
  -> docs/LLM-CONTEXT.md
  -> 작업 영역별 Wiki
  -> 해당 Controller
  -> Service
  -> Repository + Entity + DTO
  -> Security/Exception 영향
  -> 테스트 및 static/app.js 소비 코드
```

## 문서 신뢰 범위

2026-08-01의 저장소 상태를 기준으로 작성되었다. 문서와 코드가 다르면 코드를 기준으로 사실을 재검증하고 Wiki도 같은 변경에서 갱신한다.
