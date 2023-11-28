package com.example.usedAuction.dto.auction;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class AuctionTransactionBidFormDto {

    private Integer price;
}
