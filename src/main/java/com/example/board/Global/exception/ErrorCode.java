package com.example.board.Global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음"),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시판을 찾을 수 없음"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없음"),

    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT,"이미 사용중인 이메일"),
    DUPLICATE_BOARD_NAME(HttpStatus.CONFLICT,"이미 사용중인 게시판이름"),

    ACCESS_DENIED(HttpStatus.FORBIDDEN,"권한이 없음"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN,"게시글 작성 권한이 없음"),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN,"게시판 생성 권한이 없음"),

    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED,"로그인이 필요함"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,"로그인에 실패함"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST,"잘못 된 입력"),
    INVALID_BOARD_ID(HttpStatus.BAD_REQUEST,"해당 Board ID는 삭제할 수 없습니다."),

    SQL_INTERGRITY_ERROR(HttpStatus.BAD_REQUEST,"DB 참조 무결성 에러"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"알 수 없는 내부 서버 에러");

    private final HttpStatus status;
    private final String message;
}
