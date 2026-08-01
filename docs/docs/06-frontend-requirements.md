---
title: 프론트엔드 요구사항
tags: [board, frontend, requirements, product]
status: draft
---

# 프론트엔드 요구사항

이 문서는 현재 API를 기반으로 한 **프론트엔드 초안 요구사항**이다. [[07-known-issues-and-decisions#프론트엔드 착수 전 결정]]을 확정한 뒤 구현 계약으로 승격한다.

## 정보 구조와 라우트 제안

| 화면 | 제안 route | 공개 여부 | 핵심 데이터 |
|---|---|---:|---|
| 게시판 홈 | `/boards` | 공개 | 게시판 목록 |
| 게시판 글 목록 | `/boards/:boardId` | 공개 | 게시글 Page |
| 게시글 상세 | `/posts/:postId` | 공개 | 글, 반응, 댓글 Page |
| 글 작성 | `/boards/:boardId/new` | 로그인 | multipart |
| 글 수정 | `/posts/:postId/edit` | 작성자 | title/body |
| 로그인 | `/login` | 공개 | 로컬/OAuth 진입 |
| 회원가입 | `/signup` | 공개 | 계정 정보 |
| 알림 | `/notifications` 또는 popover | 로그인 | 알림 Page/미읽음 수 |
| 내 프로필 | `/me` | 로그인 | UserProfileDTO |
| 게시판 관리 | `/admin/boards` | ADMIN | 게시판 CRUD |

## 공통 레이아웃

- 상단: 서비스 로고, 게시판 진입, 로그인 사용자 메뉴, 알림 badge
- 본문: 목록/상세 라우트 outlet
- 모바일: 게시판 목록과 글 목록을 별도 단계로 분리
- 전역: toast 또는 inline alert, 접근 가능한 modal/confirm, 404/403/500 상태

## 기능 요구사항

### 인증

- 앱 초기화 중 세션 복구가 끝날 때까지 보호 화면을 확정하지 않는다.
- 로그인 성공 후 원래 접근하려던 route로 복귀한다.
- 401 재발급은 요청당 무한 반복되지 않아야 한다.
- 로그아웃은 서버 cookie 만료 성공 여부와 무관하게 로컬 인증 상태를 정리한다.
- OAuth 성공 redirect 후 `/reissue`로 사용자/토큰을 복구한다.

### 게시판과 게시글

- 게시판 선택을 URL로 표현해 새로고침/공유가 가능해야 한다.
- 목록은 페이지 번호, 빈 상태, 로딩 skeleton, 재시도를 제공한다.
- 글 상세는 본문 줄바꿈, 이미지 순서, 조회수, 작성자, 생성일을 표시한다.
- `canEdit`/`canDelete`가 true일 때만 작성자 동작을 노출한다.
- 삭제 전 확인하고 성공 후 상위 목록으로 이동하며 cache를 무효화한다.
- 작성 폼은 이미지 3개, 허용 형식, 5MB 제한을 제출 전에 안내/검증한다.
- 수정 화면은 현재 API 한계상 이미지 편집이 불가능함을 명확히 한다.

### 댓글

- 루트 댓글은 페이지 단위, 답글은 각 루트 아래 중첩 표시한다.
- 삭제 댓글은 서버가 반환한 대체 문구를 표시하고 수정/삭제 버튼을 숨긴다.
- 답글 작성 시 대상 댓글/작성자를 명확히 표시한다.
- content 공백 금지, 최대 1000자를 카운터와 함께 검증한다.
- 깊은 답글 정책이 정해질 때까지 UI는 1단계 답글만 허용하는 것을 권고한다.

### 반응

- 좋아요/싫어요 수와 내 선택 상태를 함께 표시한다.
- 낙관적 업데이트를 사용한다면 실패 시 이전 상태로 롤백한다.
- 빠른 연타를 직렬화하거나 버튼을 잠가 중복 요청을 방지한다.
- P0 백엔드 수정 전에는 기능을 노출하지 않는다.

### 알림

- 로그인 상태에서 미읽음 badge를 초기 로드하고 알림 읽음 후 갱신한다.
- 읽지 않은 항목을 시각적으로 구분한다.
- 알림 클릭 시 게시글로 이동하고 가능하면 commentId 위치를 강조한다.
- 대상 게시글이 삭제된 경우 안내와 알림 읽음 처리를 분리한다.

## API 클라이언트 요구사항

- base URL과 cookie 정책을 환경별 설정으로 분리한다.
- JSON과 multipart body를 구분하며 multipart에 `Content-Type`을 수동 지정하지 않는다.
- 오류는 `ErrorResponse.code`를 기계 판정하고 `msg`를 사용자 문구로 사용할 수 있다.
- Spring page 응답을 내부 `items/page/totalPages/totalElements` 모델로 정규화한다.
- 날짜 문자열은 timezone이 없는 `LocalDateTime`임을 감안해 표시 정책을 정한다(현재 서버 timezone: Asia/Seoul).
- 공개 상세 요청에도 token이 있으면 전송해야 서버가 권한과 `myReaction`을 계산할 수 있다.

## 상태 모델 제안

```text
auth: initializing | anonymous | authenticated | refreshing
request: idle | loading | success | empty | error
mutation: idle | submitting | success | error
```

각 목록/상세는 최소한 loading, empty/not-found, forbidden, generic-error 상태를 별도로 렌더링한다.

## 접근성/품질 기준

- 키보드만으로 메뉴, 알림, modal, 폼을 사용할 수 있어야 한다.
- 아이콘 버튼에 accessible name을 제공한다.
- 색상 외의 방식으로 읽음/선택/오류 상태를 전달한다.
- 사용자 입력 본문은 HTML로 직접 삽입하지 않는다.
- 모바일 360px부터 데스크톱까지 주요 기능이 막히지 않아야 한다.
- API mock과 실제 개발 서버 양쪽에서 핵심 흐름을 검증한다.

## MVP 완료 조건

1. 공개 게시판→글 목록→상세→댓글 탐색이 가능하다.
2. 회원가입/로그인/재발급/로그아웃이 일관되게 동작한다.
3. 로그인 사용자가 글과 댓글을 작성하고 본인 콘텐츠를 관리한다.
4. 이미지 업로드 제한과 실패가 사용자에게 설명된다.
5. 댓글 알림 확인과 읽음 처리가 가능하다.
6. 관리자가 게시판을 관리하고 일반 사용자는 접근할 수 없다.
7. 반응은 백엔드 수정 및 계약 테스트 후 제공한다.

