package com.example.usedAuction.dto.auction;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.List;

@Data
public class AuctionTransactionDto {
    private Integer auctionTransactionId;
    private Integer seller;
    private Integer buyer;
    @NotBlank(message = "제목을 입력하세요.")
    private List<AuctionTransactionImageDto> images;
    private String thumbnail;
    private String title;
    private String content;
    private Integer price;
    @NotBlank(message = "거래방식을 입력하세요.")
    private String transactionMode;
    private String location;
    private String transactionState;
    private String payment;
    private Integer viewCount=0;
    @NotBlank(message = "경매 시작일을 지정하세요")
    private Timestamp startedAt;
    @NotBlank(message = "경매 종료일을 지정하세요")
    private Timestamp finishedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
