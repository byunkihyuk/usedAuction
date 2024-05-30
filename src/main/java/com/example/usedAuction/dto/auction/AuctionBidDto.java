package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.transactionenum.AuctionBidStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class AuctionBidDto {
    @Schema(description = "입찰 번호")
    private Integer auctionBidId;
    @Schema(description = "입찰 가격")
    private Integer price;
    @Schema(description = "입찰자")
    private Integer bidder;
    @Schema(description = "입찰자 닉네임")
    private String bidderNickname;
    @Schema(description = "입찰 상태")
    private AuctionBidStateEnum auctionBidState;
    @Schema(description = "입찰 글 번호")
    private Integer auctionTransactionId;
    @Schema(description = "입찰 시간")
    private Timestamp createdAt;
    @Schema(description = "입찰 수정 시간")
    private Timestamp updatedAt;
}
