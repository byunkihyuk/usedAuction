package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionRequestTypeEnum {
    CHARGE("충전"),
    PAYMENT("결제"),
    WITHDRAWAL("출금"),
    DEPOSIT("입금");

    @JsonValue // enum 값 문자열 출력
    private final String type;
    
    @JsonCreator
    public static TransactionRequestTypeEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(TransactionRequestTypeEnum type : TransactionRequestTypeEnum.values()){
            if(type.getType().equalsIgnoreCase(value)){
                return type;
            }
        }
        throw new IllegalArgumentException("TransactionRequestTypeEnum에 일치하지 않습니다");

    }

}
