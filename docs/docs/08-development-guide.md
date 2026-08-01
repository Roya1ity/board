---
title: 개발 가이드
tags: [board, development, setup, testing]
status: current
---

# 개발 가이드

## 사전 요구사항

- JDK 21
- 로컬 MySQL, 기본 DB 이름 `board`
- OAuth를 사용할 경우 Google/Kakao client 설정
- Windows에서는 `gradlew.bat`, Unix 계열에서는 `./gradlew`

## 주요 설정

`application.yaml`은 선택적으로 프로젝트 루트 `.env` properties 파일을 읽는다.

| 키/환경 변수 | 용도 | 현재 기본/상태 |
|---|---|---|
| `spring.datasource.url` | MySQL 연결 | localhost:3306/board |
| `KAKAO_REST_API` | Kakao client/app key | 필수(OAuth 사용 시) |
| `KAKAO_SECRET` | Kakao secret | 필수(OAuth 사용 시) |
| `KAKAO_CALLBACK` | 별도 Kakao callback | 필수(해당 흐름 사용 시) |
| `GOOGLE_CLIENT_ID` | Google OAuth | 필수(사용 시) |
| `GOOGLE_CLIENT_SECRET` | Google OAuth | 필수(사용 시) |
| `APP_UPLOAD_DIR` | 이미지 저장 경로 | `./uploads` |

> [!danger] 운영 금지 설정
> 현재 DB password와 JWT secret을 그대로 사용하지 않는다. 환경 변수/secret manager로 이동하고 교체한다. `ddl-auto: update`, `show-sql: true`, DEBUG security logging도 운영 프로필에서 끈다.

## 실행과 검증

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

서버 기본 포트는 `8099`, 정적 데모는 `http://localhost:8099/`다.

## 파일 업로드

- 디렉터리는 시작 시 자동 생성된다.
- URL `/images/**`가 업로드 디렉터리에 매핑된다.
- 최대 파일 5MB, 요청 20MB, 이미지 3개다.
- 로컬 개발에서 앱 실행 기준 경로에 따라 실제 저장 위치가 달라질 수 있으므로 `APP_UPLOAD_DIR` 절대 경로 사용을 권고한다.

## 테스트 전략(추가 필요)

### 우선 통합 테스트

- 회원가입이 임의 ADMIN role을 허용하지 않는지
- 로그인 → cookie → 재발급 → 로그아웃
- 공개 글 상세의 비회원/작성자/타 사용자 조회수와 권한 필드
- 게시글 반응 생성 → 전환 → 취소 → 집계
- 다른 사용자의 게시글/댓글 수정·삭제 403
- 다른 사용자의 알림 읽음 처리 403
- 다른 게시글의 댓글을 parent로 지정한 답글 거부
- 게시글/게시판 삭제 후 댓글·반응·알림·이미지 정합성

### API 계약 테스트

- `ErrorCode.status`와 실제 HTTP status 일치
- 로그인/재발급 access token 형식 일치
- page 응답 구조 고정
- DTO 날짜/필드 이름 고정
- multipart 이미지 개수/형식/크기 오류

## 코드 변경 시 Wiki 체크리스트

- endpoint/DTO 변경 → [[03-api-reference]]
- 엔티티/제약/삭제 정책 변경 → [[02-domain-model]]
- SecurityConfig/JWT/OAuth 변경 → [[04-auth-and-security]]
- 사용자 흐름 변경 → [[05-feature-flows]]
- 해결한 문제 → [[07-known-issues-and-decisions]] 상태 갱신
- 프론트 구현 결정을 내림 → [[06-frontend-requirements]]의 가정을 확정 문구로 변경

