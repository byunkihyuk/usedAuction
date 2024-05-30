package com.example.usedAuction.dto.payment;

import com.example.usedAuction.entity.transactionenum.TransactionRequestStateEnum;
import com.example.usedAuction.entity.transactionenum.TransactionRequestTypeEnum;
import com.example.usedAuction.entity.transactionenum.UsedTransactionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PayInfoDto {
    @Schema(description = "거래정보 번호")
    private Integer payInfoId;
    @Schema(description = "판매자/사용자")
    private Integer seller;
    @Schema(description = "판매자 닉네임")
    private String sellerNickname;
    @Schema(description = "구매자")
    private Integer buyer;
    @Schema(description = "구매자 닉네임")
    private String buyerNickname;
    @Schema(description = "거래 요청 종류",allowableValues = {"충전","결제","출금","송금"})
    private TransactionRequestTypeEnum transactionRequestType; // 충전 / 결제 / 출금 / 송금
    @Schema(description = "거래 상태",allowableValues = {"승인","대기","거래중","취소"})
    private TransactionRequestStateEnum transactionRequestState; // 승인 / 대기 / 거래중 / 취소
    @Schema(description = "거래 금액")
    private Integer transactionMoney; // 증감액
    @Schema(description = "거래 종류",allowableValues = {"일반 거래글 거래","경매 거래글 입찰","입출금"})
    private UsedTransactionTypeEnum usedTransactionType; // 일반 거래글 거래 / 경매 거래글 입찰 / 입출금
    @Schema(description = "중고 글 번호")
    private Integer generalTransactionId;
    @Schema(description = "경매 글 번호")
    private Integer auctionTransactionId;
    @Schema(description = "거래 시간")
    private Timestamp transactionTime;
    @Schema(description = "거래 정보 수정 시간")
    private Timestamp transactionUpdateTime;
}
