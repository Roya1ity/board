package com.example.board.Global.exception;

public class ForbidenException extends BusinessException {
    public ForbidenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
