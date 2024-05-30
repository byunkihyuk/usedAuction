package com.example.usedAuction.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInFormDto {
    @Schema(description = "아이디(이메일)")
    private String username;
    @Schema(description = "비밀번호")
    private String password;
}
