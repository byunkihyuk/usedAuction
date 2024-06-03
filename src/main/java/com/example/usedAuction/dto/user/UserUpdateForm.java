package com.example.usedAuction.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateForm {
    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
    @Schema(description = "수정 비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String changePassword;
    @Schema(description = "닉네임")
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;
    @Schema(description = "전화번호")
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;
    @Schema(description = "주소")
    private String address;
    @Schema(description = "상세 주소")
    private String detailAddress;
}
