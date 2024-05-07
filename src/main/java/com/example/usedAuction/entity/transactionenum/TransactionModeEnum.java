package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionModeEnum {
    ONLINE("온라인"),
    DIRECT("직거래");

    @JsonValue // enum 값 문자열 출력
    private final String mode;

    @JsonCreator
    public static TransactionModeEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(TransactionModeEnum mode : TransactionModeEnum.values()){
            if(mode.getMode().equalsIgnoreCase(value)){
                return mode;
            }
        }
        throw new IllegalArgumentException("TransactionModeEnum 목록과 일치하지 않습니다");
    }


}
