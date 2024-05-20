package com.example.usedAuction.dto.general;

import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTransactionFormDto {
    private Integer generalTransactionId;
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    private String content;
    private Integer price;
    private TransactionModeEnum transactionMode;
    private String address;
    private String detailAddress;
    private TransactionStateEnum transactionState = TransactionStateEnum.SALE;
    private TransactionPaymentEnum payment;
}
