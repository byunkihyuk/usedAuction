package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private Integer highestBid;
    private Integer price;
    @NotNull(message = "거래방식을 선택하세요.")
    private TransactionModeEnum transactionMode;
    private String address;
    private String detailAddress;
    private TransactionStateEnum transactionState;
    private TransactionPaymentEnum payment;
    private Integer viewCount=0;
    @NotBlank(message = "경매 시작일을 지정하세요")
    private Timestamp startedAt;
    @NotBlank(message = "경매 종료일을 지정하세요")
    private Timestamp finishedAt;
    private Boolean author=false;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
