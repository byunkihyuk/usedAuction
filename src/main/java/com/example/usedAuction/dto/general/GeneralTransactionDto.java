package com.example.usedAuction.dto.general;

import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTransactionDto {

    @Schema(description = "중고 거래 글 번호")
    private Integer generalTransactionId;
    @Schema(description = "판매자")
    private Integer seller;
    @Schema(description = "구매자")
    private Integer buyer;
    @Schema(description = "중고 거래 글 썸네일")
    private String thumbnail;
    @Schema(description = "중고 거래 글 이미지 목록")
    private List<GeneralTransactionImageDto> images;
    @Schema(description = "제목")
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    @Schema(description = "내용")
    private String content;
    @Schema(description = "가격")
    private Integer price;
    @Schema(description = "거래 방식",allowableValues = {"온라인","직거래"})
    private TransactionModeEnum transactionMode;
    @Schema(description = "주소")
    private String address;
    @Schema(description = "상세 주소")
    private String detailAddress;
    @Schema(description = "판매 상태",allowableValues = {"판매중","거래중","판매완료"})
    private TransactionStateEnum transactionState;
    @Schema(description = "결제 방식",allowableValues = {"온라인","직거래"})
    private TransactionPaymentEnum payment;
    @Schema(description = "조회수")
    private Integer viewCount=0;
    @Schema(description = "글 등록 시간")
    private Timestamp createdAt;
    @Schema(description = "글 수정 시간")
    private Timestamp updatedAt;
    @Schema(description = "작성자")
    private Boolean author = false;
}
