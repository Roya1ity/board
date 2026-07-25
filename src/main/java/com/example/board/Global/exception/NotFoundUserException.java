package com.example.board.Global.exception;

public class NotFoundUserException extends BusinessException {
    public NotFoundUserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
