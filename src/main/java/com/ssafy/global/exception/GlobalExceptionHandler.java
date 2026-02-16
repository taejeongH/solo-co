package com.ssafy.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

        ErrorCode code = e.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .status(code.getStatus())
                .errorCode(code.getErrorCode())
                .message(code.getMessage())
                .detail(e.getDetail())
                .build();

        return ResponseEntity.status(code.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        ErrorCode code = ErrorCode.INTERNAL_ERROR;

        ErrorResponse response = ErrorResponse.builder()
                .status(code.getStatus())
                .errorCode(code.getErrorCode())
                .message(code.getMessage())
                .detail(e.getMessage())
                .build();

        return ResponseEntity.status(code.getStatus()).body(response);
    }
}
