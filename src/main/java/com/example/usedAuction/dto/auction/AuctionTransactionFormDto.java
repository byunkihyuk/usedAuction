package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
public class AuctionTransactionFormDto {
    @Schema(description = "글 번호")
    private Integer auctionTransactionId;
    @Schema(description = "글 제목")
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    @Schema(description = "글 내용")
    private String content;
    @Schema(description = "가격")
    @NotBlank(message = "가격을 입력하세요.")
    private Integer price;
    @Schema(description = "판매자 번호",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer seller;
    @Schema(description = "거래 방식",allowableValues = {"온라인","직거래"})
    @NotNull(message = "거래방식을 입력하세요.")
    private TransactionModeEnum transactionMode;
    @Schema(description = "글 아이디")
    private String address;
    @Schema(description = "글 아이디")
    private String detailAddress;
    @Schema(description = "글 상태",allowableValues = {"판매중","거래중","거래완료"})
    private TransactionStateEnum transactionState;
    @Schema(description = "결제 방식",allowableValues = {"온라인","직거래"})
    private TransactionPaymentEnum payment;
    @Schema(description = "경매 시작일")
    private Timestamp startedAt;
    @Schema(description = "경매 종료일")
    private Timestamp finishedAt;
}
