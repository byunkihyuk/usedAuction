package com.example.usedAuction.dto.general;

import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTransactionFormDto {
    @Schema(description = "제목")
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    @Schema(description = "내용")
    private String content;
    @NotBlank(message = "가격을 입력하세요.")
    @Schema(description = "가격")
    private Integer price;
    @Schema(description = "거래 방식",allowableValues = {"온라인","직거래"})
    @NotBlank(message = "거래 방식을 선택하세요.")
    private TransactionModeEnum transactionMode;
    @Schema(description = "주소")
    private String address;
    @Schema(description = "상세 주소")
    private String detailAddress;
    @Schema(description = "판매 상태",allowableValues = {"판매중","거래중","판매완료"})
    private TransactionStateEnum transactionState = TransactionStateEnum.SALE;
    @Schema(description = "결제 방식",allowableValues = {"온라인","직거래"})
    @NotBlank(message = "결제 방식을 선택하세요.")
    private TransactionPaymentEnum payment;
}
