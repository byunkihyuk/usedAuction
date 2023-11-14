package com.example.usedAuction.dto.auction;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
public class AuctionTransactionFormDto {
    private Integer auctionTransactionId;
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    private String content;
    private Integer price;
    @NotBlank(message = "거래방식을 입력하세요.")
    private String transactionMode;
    private String location;
    private String transactionState;
    private String payment;
    private Timestamp startedAt;
    private Timestamp finishedAt;
}
