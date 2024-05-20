package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.transactionenum.AuctionBidStateEnum;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class AuctionBidDto {
    private Integer auctionBidId;

    private Integer price;

    private Integer bidder;

    private String bidderNickname;

    private AuctionBidStateEnum auctionBidState;

    private Integer auctionTransactionId;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
