package com.ssafy.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private int status; // HTTP Status
    private String errorCode; // 내부 서비스 에러 코드 (e.g. 4011)
    private String message; // 유저에게 노출할 메시지
    private String detail; // 개발용 상세 내용 (optional)
}
