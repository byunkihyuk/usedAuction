package com.example.usedAuction.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorEnum {
    // 회원 가입
    SIGN_UP_ERROR(HttpStatus.BAD_REQUEST,"회원가입 에러"),
    SIGN_IN_ERROR(HttpStatus.UNAUTHORIZED,"없는 이메일이거나 비밀번호가 틀렸습니다"),
    FORBIDDEN_ERROR(HttpStatus.FORBIDDEN,"권한이 없습니다"),
    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED,"로그인하지 않았거나 유효하지 않은 토큰입니다"),
    // 일반 거래 글
    GENERAL_TRANSACTION_POST_ERROR(HttpStatus.BAD_REQUEST,"일반 거래글 등록 에러"),
    //경매 거래 글
    NOT_FOUND_AUCTION_TRANSACTION(HttpStatus.BAD_REQUEST,"없는 경매 거래 글 입니다."),
    FAIL_BID(HttpStatus.BAD_REQUEST,"입찰에 실패했습니다."),
    // 거래 글 공통
    IMAGE_UPDATE_ERROR(HttpStatus.BAD_REQUEST," 이미지 수정 에러"),
    IMAGE_MAX_COUNT(HttpStatus.BAD_REQUEST,"이미지는 10개 이하로 업로드 가능합니다."),
    IMAGE_DELETE_ERROR(HttpStatus.BAD_REQUEST,"이미지 삭제 에러"),
    IMAGE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST,"이미지 업로드 에러"),
    NOT_FOUND_USER(HttpStatus.BAD_REQUEST,"없는 사용자 입니다."),
    NOT_FOUND_CHATROOM(HttpStatus.BAD_REQUEST,"없는 채팅방 입니다." ), 
    CHAT_MESSAGE_SEND_ERROR(HttpStatus.BAD_REQUEST,"채팅 메시지 저장 에러" );

    private final HttpStatus httpStatus;
    private final String message;
}
