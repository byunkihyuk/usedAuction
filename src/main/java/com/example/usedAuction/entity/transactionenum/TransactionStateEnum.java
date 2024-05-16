package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStateEnum {
    SALE("판매중"),
    PROGRESS("거래중"),
    COMPLETE("판매완료");

    @JsonValue // enum 값 문자열 출력
    private final String state;

    @JsonCreator
    public static TransactionStateEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(TransactionStateEnum state : TransactionStateEnum.values()){
            if(state.getState().equalsIgnoreCase(value)){
                return state;
            }
        }
        throw new IllegalArgumentException("TransactionStateEnum 목록과 일치하지 않습니다");
    }
}
