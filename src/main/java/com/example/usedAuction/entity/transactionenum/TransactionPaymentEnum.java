package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionPaymentEnum {
    // 온라인 결제, 직거래 계좌이체
    ONLINE("온라인"),
    DIRECT("직거래");

    @JsonValue // enum 값 문자열 출력
    private final String type;

    @JsonCreator
    public static TransactionPaymentEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(TransactionPaymentEnum type : TransactionPaymentEnum.values()){
            if(type.getType().equalsIgnoreCase(value)){
                return type;
            }
        }
        throw new IllegalArgumentException("TransactionPaymentEnum 목록과 일치하지 않습니다");

    }
}
