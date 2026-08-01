---
title: API 레퍼런스
tags: [board, api, frontend-contract]
status: current-as-is
---

# API 레퍼런스

기준 URL은 개발 환경 `http://localhost:8099`다. JSON 요청은 `Content-Type: application/json`, 인증 요청은 `Authorization: Bearer <access-token>`을 사용한다. refresh cookie를 위해 브라우저 요청은 `credentials: include`가 필요하다.

> [!warning] 현재 구현 계약
> 이 문서는 코드 그대로를 기록한다. 특히 반응 경로의 `reation` 오타와 재발급 응답 token prefix 차이를 임의로 교정하지 않았다.

## 공통 응답

성공 메시지:

```json
{ "status": "OK", "msg": "삭제 완료" }
```

오류 목표 형식:

```json
{
  "code": "POST_NOT_FOUND",
  "msg": "게시글을 찾을 수 없음",
  "time": "2026-08-01T12:00:00"
}
```

페이지 응답은 대략 다음 형태다.

```json
{
  "content": [],
  "page": { "size": 10, "number": 0, "totalElements": 0, "totalPages": 0 }
}
```

## 인증

| Method | Path | 인증 | 요청 | 응답/비고 |
|---|---|---:|---|---|
| POST | `/api/auth/new` | 공개 | `SignupRequest` | `IngestResult` |
| POST | `/api/auth/login` | 공개 | `LoginRequest` | `UserResponse`, refresh cookie 설정 |
| POST | `/api/auth/logout` | 공개 | 없음 | 200, refresh cookie 만료 |
| POST | `/api/auth/reissue` | 공개(cookie 필요) | 없음 | `TokenResponse` |
| GET | `/oauth2/authorization/{google|kakao}` | 공개 | 브라우저 redirect | Spring OAuth2 시작 |
| GET | `/api/oauth/kakao/login` | 공개 | 없음 | 별도 Kakao OAuth 시작(302) |
| GET | `/api/oauth/kakao/callback` | 공개 | `code,state` query | `UserResponse` 의도 |

`SignupRequest`:

```json
{ "email": "user@example.com", "pw": "password8", "nick": "사용자", "role": "user" }
```

`LoginRequest`:

```json
{ "email": "user@example.com", "pw": "password8" }
```

`UserResponse`/`TokenResponse` 필드: `id`, `email`, `nick`, `accessToken`, `refreshToken`, `role`.

- 로그인 응답의 `accessToken`은 `Bearer ` prefix를 포함한다.
- 재발급 응답의 `accessToken`은 prefix를 포함하지 않는다.
- refresh token은 HttpOnly cookie에도 들어가지만 응답 body에도 노출된다.

## 게시판

| Method | Path | 인증/권한 | 요청 | 응답 |
|---|---|---|---|---|
| GET | `/api/board/all` | 공개 | - | `BoardResponse[]` |
| POST | `/api/board/new` | 로그인 | `BoardRequest` | `BoardResponse` |
| PUT | `/api/board/{id}/update` | ADMIN | `BoardRequest` | `BoardResponse` |
| DELETE | `/api/board/{id}/delete` | ADMIN | - | `IngestResult` |

```json
// BoardRequest
{ "name": "자유게시판", "description": "자유롭게 이야기합니다." }

// BoardResponse
{ "id": 1, "name": "자유게시판", "description": "...", "createAt": "2026-08-01T10:00:00" }
```

## 게시글

| Method | Path | 인증/권한 | 요청 | 응답 |
|---|---|---|---|---|
| GET | `/api/post/all` | 공개 | - | `PostDTO[]` |
| GET | `/api/post/{boardId}/all?page=0&size=10` | 공개 | pageable | `Page<PostDTO>` |
| GET | `/api/post/{id}` | 공개 | - | `PostDTO` |
| POST | `/api/post/{boardId}/new` | 로그인 | multipart | `PostDTO` |
| PUT | `/api/post/{id}/update` | 작성자 | `PostRequest` JSON | `PostDTO` |
| DELETE | `/api/post/{id}/delete` | 작성자 | - | `IngestResult` |

작성 multipart:

- `post`: `application/json` part, `{ "title": "제목", "body": "본문" }`
- `images`: 선택, 동일 key 반복, 최대 3개
- 허용 MIME/확장자: png, jpeg/jpg, gif, webp
- 개별 파일 최대 5MB, 전체 요청 최대 20MB

`PostDTO`:

```json
{
  "id": 10,
  "title": "제목",
  "user": "작성자 닉네임",
  "board": "자유게시판",
  "body": "본문",
  "viewCount": 3,
  "images": [{ "id": 1, "url": "/images/file.jpg", "originalName": "photo.jpg", "sortOrder": 0 }],
  "like": 2,
  "dislike": 0,
  "myReaction": "LIKE",
  "createAt": "2026-08-01T10:00:00",
  "canEdit": true,
  "canDelete": true
}
```

목록/작성/수정 응답은 변환 overload 차이로 반응 및 권한 필드가 상세 응답과 다를 수 있다. 프론트는 상세 응답을 권한·반응의 기준으로 사용한다.

## 댓글

| Method | Path | 인증/권한 | 요청 | 응답 |
|---|---|---|---|---|
| GET | `/api/comment/{postId}/list?page=0&size=10` | 공개 | pageable | `Page<CommentResponse>` |
| POST | `/api/comment/{postId}/new` | 로그인 | `CommentCreateRequest` | `CommentResponse` |
| PUT | `/api/comment/{commentId}/update` | 작성자 | `CommentCreateRequest` | `CommentResponse` |
| DELETE | `/api/comment/{commentId}/delete` | 작성자 | - | 200, body 없음 |

```json
// 루트 댓글은 parentId null, 답글은 부모 ID
{ "content": "댓글 내용", "parentId": null }
```

`content`는 필수이고 최대 1000자다. `CommentResponse`는 `id`, `author`, `content`, `deleted`, `createdAt`, `canEdit`, `canDelete`, 재귀적인 `children`을 반환한다.

## 반응

| Method | Path | 인증 | 요청 | 응답 |
|---|---|---:|---|---|
| POST | `/api/reation/post/{postId}` | 로그인 | `{ "type": "LIKE" }` 또는 `DISLIKE` | `ReactionResponse` |
| POST | `/api/reation/comment/{commentId}` | 로그인 | `{ "type": "LIKE" }` 또는 `DISLIKE` | `ReactionResponse` |

`ReactionResponse`: `{ "likeCount": 1, "dislikeCount": 0, "myReaction": "LIKE" }`.

동일 타입을 다시 보내면 해당 반응 행을 삭제해 취소하고, 다른 타입이면 전환한다. 게시글 상세은 `like`, `dislike`, `myReaction`을, 댓글 응답은 `likeCount`, `dislikeCount`, `myReaction`을 포함한다.

## 알림

| Method | Path | 인증 | 응답 |
|---|---|---:|---|
| GET | `/api/notify/list?page=0&size=10` | 로그인 | `Page<NotificationResponse>` |
| GET | `/api/notify/unreads` | 로그인 | `{ "count": 3 }` |
| PUT | `/api/notify/{id}/read` | 소유자 | 200, body 없음 |

`NotificationResponse`: `id`, `type`, `message`, `actor`, `postId`, `commentId`, `read`, `createdAt`.

## 프로필

| Method | Path | 인증 | 응답 |
|---|---|---:|---|
| GET | `/api/user/me` | 로그인 | `UserProfileDTO` |

응답 필드: `name`, `birth`, `number`, `createAt`. 수정 API는 없다.
