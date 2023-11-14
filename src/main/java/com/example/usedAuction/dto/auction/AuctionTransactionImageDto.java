package com.example.usedAuction.dto.auction;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AuctionTransactionImageDto {
    private Integer auctionImageId;
    private Integer imageSeq;
    private String originName;
    private String imageName;
    private String uploadUrl;
    private Timestamp createdAt;
    private Integer auctionTransactionId;
}
