package com.example.usedAuction.entity.auction;

import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class AuctionBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_bid_id")
    private Integer auctionBidId;

    private Integer price;

    @ManyToOne
    @JoinColumn(name = "bidder_id")
    private User bidderId;

    @ManyToOne
    @JoinColumn(name = "auction_transaction_id")
    private AuctionTransaction auctionTransactionId;

    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
