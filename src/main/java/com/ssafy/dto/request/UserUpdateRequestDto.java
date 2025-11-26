package com.ssafy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    @Schema(description = "새 닉네임", example = "허태정")
    private String nickname;

    @Schema(description = "새 비밀번호 (선택)", example = "newpassword123")
    private String newPassword;

    @Schema(description = "프로필 설명", example = "여행 좋아하는 개발자")
    private String profile;
}
