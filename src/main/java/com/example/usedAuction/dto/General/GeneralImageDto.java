package com.example.usedAuction.dto.General;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class GeneralImageDto {
    private Integer generalImageId;
    private Integer imageSeq;
    private String originUrl;
    private String imageUrl;
    private String uploadUrl;
    private Timestamp createdAt;
    private Integer generalTransactionId;
}
