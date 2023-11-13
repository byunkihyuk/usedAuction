package com.example.usedAuction.dto.General;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class GeneralTransactionImageDto {
    private Integer generalImageId;
    private Integer imageSeq;
    private String originName;
    private String imageName;
    private String uploadUrl;
    private Timestamp createdAt;
    private Integer generalTransactionId;
}
