package com.example.usedAuction.dto.user;

import com.example.usedAuction.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpFormDto {

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,}$",message = "영문과 숫자를 포함 4자이상 입력")
    private String username;
    @Pattern(regexp = "^[a-zA-Z0-9]{8,}$",message = "영문과 숫자를 포함 8자이상 입력")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
    @NotBlank(message = "닉네임을 입력해주세요")
    private String nickname;
    @NotBlank(message = "전화번호를 입력해주세요")
    private String phone;
    private String address;
    private String detailAddress;

}


