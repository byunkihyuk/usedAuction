package com.example.usedAuction.dto.payment;

import com.example.usedAuction.entity.transactionenum.TransactionRequestStateEnum;
import com.example.usedAuction.entity.transactionenum.TransactionRequestTypeEnum;
import com.example.usedAuction.entity.transactionenum.UsedTransactionTypeEnum;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PayInfoDto {
    private Integer payInfoId;
    private Integer seller;
    private String sellerNickname;
    private Integer buyer;
    private String buyerNickname;
    private TransactionRequestTypeEnum transactionRequestType; // 충전 / 결제 / 출금 / 송금
    private TransactionRequestStateEnum transactionRequestState; // 승인 / 대기 / 취소
    private Integer transactionMoney; // 증감액
    private UsedTransactionTypeEnum usedTransactionType; // 일반 거래글 거래 / 경매 거래글 입찰 / 입출금
    private Integer generalTransactionId;
    private Integer auctionTransactionId;
    private Timestamp transactionTime;
    private Timestamp transactionUpdateTime;
}
