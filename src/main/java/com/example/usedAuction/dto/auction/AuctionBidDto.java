package com.example.usedAuction.dto.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

@Data
public class AuctionBidDto {
    private Integer auctionBidId;

    private Integer price;

    private Integer bidderId;

    private Integer auctionTransactionId;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
