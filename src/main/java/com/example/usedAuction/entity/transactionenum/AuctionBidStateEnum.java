package com.example.usedAuction.entity.transactionenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

@RequiredArgsConstructor
@Getter
public enum AuctionBidStateEnum {
    // 입찰, 대기, 승인
    BID("입찰"),
    WAIT("대기"),
    APPROVE("승인"),
    CANCEL("취소");

    @JsonValue // enum 값 문자열 출력
    private final String type;

    @JsonCreator
    public static AuctionBidStateEnum from(String value){
        if(value.isEmpty()){
            return null;
        }
        for(AuctionBidStateEnum mode : AuctionBidStateEnum.values()){
            if(mode.getType().equalsIgnoreCase(value)){
                return mode;
            }
        }
        throw new IllegalArgumentException("AuctionBidStateEnum 목록과 일치하지 않습니다");
    }
}
