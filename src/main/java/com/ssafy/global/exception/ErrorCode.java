package com.ssafy.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(400, "4000", "잘못된 요청입니다."),
    INVALID_INVITE_CODE(400, "4001", "유효하지 않은 초대 코드입니다."),
    PROJECT_MEMBER_ALREADY_EXISTS(400, "4002", "이미 프로젝트에 참여한 사용자입니다."),
    PLACE_REQUIRED_FOR_AI(400, "4003", "AI 추천 기능을 사용하려면 최소 1개 이상의 장소가 필요합니다."),

    // 401 Unauthorized
    INVALID_LOGIN(401, "4010", "등록되지 않은 이메일 또는 비밀번호가 잘못 입력되었습니다."),
    EXPIRED_TOKEN(401, "4011", "만료된 토큰입니다."),
    INVALID_TOKEN(401, "4012", "유효하지 않은 토큰입니다."),

    // 403 Forbidden
    FORBIDDEN(403, "4030", "접근 권한이 없습니다."),
    PROJECT_OWNER_ONLY(403, "4031", "해당 작업은 OWNER만 수행할 수 있습니다."),
    PROJECT_OWNER_CANNOT_REMOVE_SELF(403, "4032", "OWNER는 본인을 강퇴할 수 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(404, "4040", "사용자를 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(404, "4041", "존재하지 않는 프로젝트입니다."),
    POST_NOT_FOUND(404, "4042", "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(404, "4043", "존재하지 않는 댓글입니다."),
    VOTE_NOT_FOUND(404, "4044", "존재하지 않는 투표입니다."),
    PLACE_NOT_FOUND(404, "4045", "존재하지 않는 장소입니다."),
    
    // 409 Conflict
    USERNAME_ALREADY_EXISTS(409, "4090", "이미 존재하는 사용자명입니다."),
    VOTE_ALREADY_EXISTS(409, "4091", "이미 해당 투표에 참여했습니다."),
    PLACE_ALREADY_EXISTS(409, "4092", "이미 추가된 장소입니다."),

    // 500 Internal Server Error
    INTERNAL_ERROR(500, "5000", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String errorCode;
    private final String message;
}
