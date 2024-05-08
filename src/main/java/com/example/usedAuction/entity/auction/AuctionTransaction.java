package com.example.usedAuction.entity.auction;

import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import lombok.Data;
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

    @Column(name = "transaction_mode")
    @Enumerated(EnumType.STRING)
    private TransactionModeEnum transactionMode;

    private String location;

    @Column(name = "transaction_sate")
    @Enumerated(EnumType.STRING)
    private TransactionStateEnum transactionState;

    @Enumerated(EnumType.STRING)
    private TransactionPaymentEnum payment;

    @Column(columnDefinition = "integer default 0")
    private Integer viewCount=0;

    @Column(name = "started_at")
    private Timestamp startedAt;

    @Column(name = "finished_at")
    private Timestamp finishedAt;

    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "auctionTransactionId", cascade = CascadeType.REMOVE)
    private List<AuctionTransactionImage> auctionTransactionImageList = new ArrayList<>();

    @OneToMany(mappedBy = "auctionTransactionId", cascade = CascadeType.REMOVE)
    private List<AuctionBid> auctionBidList = new ArrayList<>();

    @OneToMany(mappedBy = "auctionTransactionId", cascade = CascadeType.REFRESH)
    private List<PayInfo> payInfoList = new ArrayList<>();
}
