---
title: 프로젝트 개요
tags: [board, overview, product]
status: current
---

# 프로젝트 개요

## 제품 정의

Board는 여러 게시판을 제공하는 소규모 커뮤니티 서비스다. 방문자는 게시판·게시글·댓글을 읽을 수 있고, 로그인 사용자는 글/댓글을 작성하고 자신의 콘텐츠를 수정·삭제하며 알림을 확인할 수 있다. 관리자는 게시판을 관리한다는 의도가 코드에 있으나, 생성 권한은 현재 완전히 강제되지 않는다.

## 현재 제공 기능

- 이메일/비밀번호 회원가입, 로그인, 로그아웃, 토큰 재발급
- Google/Kakao Spring Security OAuth2 로그인 및 별도 Kakao OAuth 흐름
- 게시판 생성·목록·수정·삭제
- 게시글 목록·상세·작성·수정·삭제, 최대 3개 이미지 업로드
- 루트 댓글과 답글, 작성자 수정, 소프트 삭제
- 댓글/답글 생성 시 대상 사용자 알림, 읽음 처리, 미읽음 수
- 게시글 좋아요/싫어요 모델과 API 골격
- 사용자 자신의 프로필 조회
- 서버에서 직접 제공하는 단일 페이지 정적 클라이언트

## 기술 스택

| 영역 | 현재 선택 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.14 |
| 웹 | Spring MVC, Bean Validation |
| 인증 | Spring Security, JWT(JJWT 0.12.6), OAuth2/OIDC |
| 데이터 | Spring Data JPA, Hibernate, MySQL |
| 빌드 | Gradle Wrapper |
| 파일 | 로컬 파일시스템 `uploads`, `/images/**` 정적 매핑 |
| 프론트 | Vanilla HTML/CSS/JavaScript(현재 데모) |
| 테스트 | Spring Boot 기본 컨텍스트 테스트만 존재 |

## 핵심 사용자 역할

| 역할 | 의도된 능력 | 현재 주의점 |
|---|---|---|
| 비회원 | 게시판/글/댓글 조회 | 글 상세와 댓글에서 로그인 사용자 정보는 선택적 |
| `USER` | 글·댓글·반응·알림, 본인 콘텐츠 관리 | 반응 저장은 현재 결함 있음 |
| `ADMIN` | 사용자 기능 + 게시판 관리 | 게시판 생성에는 `ADMIN` 검사 없음 |
| `GUEST` | 코드상 역할만 존재 | 별도 기능 정책 없음 |

## 성공 기준(향후 제품 요구사항)

- 새 프론트엔드는 [[03-api-reference]]의 현재 계약을 그대로 가정하기 전에 [[07-known-issues-and-decisions]]의 P0 항목을 해결한다.
- 공개 화면은 토큰 없이 조회 가능하고, 사용자별 권한 UI는 서버의 `canEdit`/`canDelete`를 따른다.
- 로그인 복구는 HttpOnly refresh cookie로 수행하고 access token 만료 시 한 번만 재발급 후 요청을 재시도한다.
- 페이지네이션, 빈 상태, 로딩, 오류, 인증 만료 상태를 모든 목록 화면에서 명시적으로 처리한다.

## 관련 문서

- 구조: [[01-architecture]]
- 데이터: [[02-domain-model]]
- 프론트엔드 범위: [[06-frontend-requirements]]

