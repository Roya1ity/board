package com.example.board.Global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String msg;
    private LocalDateTime time;

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(),errorCode.getMessage(),LocalDateTime.now());
    }
}
