---
title: Board LLM Wiki
aliases:
  - Home
  - Board Wiki
tags:
  - board
  - wiki
  - llm-context
wiki_version: 1.0
source_snapshot: 2026-08-01
---

# Board LLM Wiki

이 폴더는 현재 백엔드 코드를 기준으로 만든 **사실 기반 프로젝트 Wiki**다. Obsidian에서는 이 폴더를 Vault로 열거나 기존 Vault에 포함할 수 있고, Codex/LLM은 이 문서를 프로젝트 탐색의 시작점으로 사용할 수 있다.

> [!important] 문서 해석 원칙
> `현재 구현`은 코드에서 확인된 사실이고, `목표 요구사항`은 프론트엔드 제작 또는 백엔드 보완을 위한 제안이다. 둘이 충돌하면 [[07-known-issues-and-decisions|알려진 문제와 결정 필요 사항]]을 먼저 확인한다.

## 빠른 탐색

| 목적 | 문서 |
|---|---|
| 프로젝트를 빠르게 이해 | [[00-project-overview]] |
| 패키지와 요청 흐름 파악 | [[01-architecture]] |
| 엔티티와 관계 확인 | [[02-domain-model]] |
| 프론트엔드 API 연동 | [[03-api-reference]] |
| JWT/OAuth/권한 처리 | [[04-auth-and-security]] |
| 기능별 시퀀스 이해 | [[05-feature-flows]] |
| 화면과 UX 요구사항 수립 | [[06-frontend-requirements]] |
| 버그·모호성·기술부채 확인 | [[07-known-issues-and-decisions]] |
| 실행·설정·검증 | [[08-development-guide]] |
| LLM용 압축 컨텍스트 | [[LLM-CONTEXT]] |

## 시스템 한 줄 요약

사용자가 게시판의 글과 계층형 댓글을 조회·작성하고, 댓글 알림과 게시글 반응을 사용하는 Spring Boot 기반 커뮤니티 애플리케이션이다. 일반 조회는 공개이며 쓰기 작업과 개인 데이터는 JWT 인증이 필요하다.

## 문서 유지 규칙

1. 컨트롤러/DTO/엔티티가 바뀌면 [[03-api-reference]]와 [[02-domain-model]]을 함께 갱신한다.
2. 인증 정책이 바뀌면 [[04-auth-and-security]]와 프론트엔드 세션 요구사항을 함께 갱신한다.
3. 미해결 구현 문제는 지우지 말고 [[07-known-issues-and-decisions]]에서 상태를 변경한다.
4. 코드로 확인되지 않은 내용은 `제안`, `가정`, `결정 필요` 중 하나로 표시한다.

## 원본 코드 기준점

- 애플리케이션: `src/main/java/com/example/board`
- 설정: `src/main/resources/application.yaml`
- 기존 정적 클라이언트: `src/main/resources/static`
- 빌드: `build.gradle`
- 집중 분석 시작점: `reaction/ReactionService.java`

