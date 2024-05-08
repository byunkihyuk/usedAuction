package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionRequestStateEnum {
    APPROVE("승인"),
    WAIT("대기"),
    PROGRESS("거래중"),
    CANCEL("취소");

    @JsonValue // enum 값 문자열 출력
    private final String type;

    @JsonCreator
    public static TransactionRequestStateEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(TransactionRequestStateEnum state : TransactionRequestStateEnum.values()){
            if(state.getType().equalsIgnoreCase(value)){
                return state;
            }
        }
        throw new IllegalArgumentException("TransactionRequestStateEnum 목록과 일치하지 않습니다");
    }
}
