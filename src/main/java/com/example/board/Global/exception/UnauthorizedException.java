package com.example.board.Global.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
