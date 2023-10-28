package com.example.usedAuction.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorEnum {
    // 회원가입
    SIGN_UP_FAIL(HttpStatus.BAD_REQUEST,"회원가입 실패"),
    SIGN_IN_FAIL(HttpStatus.UNAUTHORIZED,"없는 이메일이거나 비밀번호가 틀렸습니다"),
    FORBIDDEN_ERROR(HttpStatus.FORBIDDEN,"권한이 없습니다"),
    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED,"로그인하지 않았거나 유효하지 않은 토큰입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
