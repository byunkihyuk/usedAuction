package com.example.usedAuction.entity.auction;

import com.example.usedAuction.entity.general.GeneralTransactionImage;
import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuctionTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_transaction_id")
    private Integer auctionTransactionId;

    @ManyToOne
    @JoinColumn(name = "seller")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "buyer")
    private User buyer;
    private String thumbnail;
    private String title;

    private String content;

    private Integer price;

    private String transaction_mode;

    private String location;

    private String transaction_state;

    private String payment;

    @Column(columnDefinition = "integer default 0")
    private Integer viewCount;

    @Column(name = "started_at",nullable = false)
    private Timestamp startedAt;

    @Column(name = "finished_at",nullable = false)
    private Timestamp finishedAt;

    @CreatedDate
    @Column(name = "created_at",nullable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "auctionTransactionId", cascade = CascadeType.REMOVE)
    private List<AuctionTransactionImage> auctionTransactionImageList = new ArrayList<>();
}
