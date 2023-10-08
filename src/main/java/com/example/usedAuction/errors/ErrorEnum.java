package com.example.usedAuction.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorEnum {
    // 회원가입
    SIGN_UP_ERROR(HttpStatus.BAD_REQUEST,"회원가입 실패 에러");

    private final HttpStatus httpStatus;
    private final String message;
}
