package com.example.usedAuction.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MailAuthDto {
    @Schema(description = "아이디(이메일)")
    private String mail;
    @Schema(description = "인증번호")
    private String Number;
}
