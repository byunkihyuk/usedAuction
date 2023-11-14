package com.example.usedAuction.entity.auction;

import com.example.usedAuction.entity.general.GeneralTransaction;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuctionTransactionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_image_id")
    private Integer auctionImageId;
    @Column(name = "image_seq")
    private Integer imageSeq;
    @Column(name = "origin_url")
    private String originName;
    @Column(name = "image_url")
    private String imageName;
    @Column(name = "upload_url")
    private String uploadUrl;
    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "auction_transaction_id")
    private AuctionTransaction auctionTransactionId;
}
