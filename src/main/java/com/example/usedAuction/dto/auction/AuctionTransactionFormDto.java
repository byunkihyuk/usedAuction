package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
public class AuctionTransactionFormDto {
    private Integer auctionTransactionId;
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    private String content;
    private Integer price;
    @NotNull(message = "거래방식을 입력하세요.")
    private TransactionModeEnum transactionMode;
    private String location;
    private TransactionStateEnum transactionState;
    private TransactionPaymentEnum payment;
    private Timestamp startedAt;
    private Timestamp finishedAt;
}
