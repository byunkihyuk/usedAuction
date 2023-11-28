package com.example.usedAuction.dto.general;

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
    @NotBlank(message = "거래방식을 입력하세요.")
    private String transactionMode;
    private String location;
    private String transactionState = "판매중";
    private String payment;
}
