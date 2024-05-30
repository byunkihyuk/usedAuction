package com.example.usedAuction.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpFormDto {

    @Schema(description = "아이디(이메일)")
    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",message = "이메일 형식에 맞게 입력해주세요.")
    private String username;
    @Schema(description = "비밀번호")
    @Pattern(regexp = "^[a-zA-Z0-9]{8,}$",message = "비밀번호 영문과 숫자를 포함 8자이상 입력해주세요.")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
    @Schema(description = "닉네임")
    @NotBlank(message = "닉네임을 입력해주세요")
    private String nickname;
    @Schema(description = "전화번호")
    @NotBlank(message = "전화번호를 입력해주세요")
    private String phone;
    @Schema(description = "주소")
    private String address;
    @Schema(description = "상세주소")
    private String detailAddress;

}


