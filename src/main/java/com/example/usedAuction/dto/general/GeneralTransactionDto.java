package com.example.usedAuction.dto.general;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTransactionDto {

    private Integer generalTransactionId;
    private Integer seller;
    private Integer buyer;
    private String thumbnail;
    private List<GeneralTransactionImageDto> images;
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    private String content;
    private Integer price;
    @NotBlank(message = "거래방식을 입력하세요.")
    private String transactionMode;
    private String location;
    private String transactionState;
    private String payment;
    private Integer viewCount=0;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}