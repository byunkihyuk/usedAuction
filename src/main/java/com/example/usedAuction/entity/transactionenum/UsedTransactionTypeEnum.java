package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsedTransactionTypeEnum {
    GENERAL_TRANSACTION("일반 거래 글"),
    AUCTION_TRANSACTION("경매 거래 글"),
    MONEY_TRANSACTION("입출금");

    @JsonValue // enum 값 문자열 출력
    private final String type;

    @JsonCreator
    public static UsedTransactionTypeEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(UsedTransactionTypeEnum state : UsedTransactionTypeEnum.values()){
            if(state.getType().equalsIgnoreCase(value)){
                return state;
            }
        }
        throw new IllegalArgumentException("UsedTransactionTypeEnum 목록과 일치하지 않습니다");
    }
}
