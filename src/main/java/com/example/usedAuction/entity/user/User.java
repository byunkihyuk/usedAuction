package com.example.usedAuction.entity.user;

import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.chat.ChattingMessage;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.payment.PayInfo;
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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String detailAddress;

    @ColumnDefault(value = "0")
    private Integer money;

    @CreatedDate
    @Column(name ="created_at" ,nullable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name ="updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.REMOVE)
    private List<GeneralTransaction> generalTransactionSellList = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.REMOVE)
    private List<AuctionTransaction> auctionTransactionSellList = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.REMOVE)
    private List<GeneralTransaction> generalTransactionBuyList = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.REMOVE)
    private List<AuctionTransaction> auctionTransactionBuyList = new ArrayList<>();

    @OneToMany(mappedBy = "bidder", cascade = CascadeType.REMOVE)
    private List<AuctionBid> auctionBidList = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    private List<ChattingMessage> chatSenderList = new ArrayList<>();

    @OneToMany(mappedBy = "seller")
    private List<PayInfo> payInfoSenderList = new ArrayList<>();

    @OneToMany(mappedBy = "buyer")
    private List<PayInfo> payInfoReceiverList = new ArrayList<>();
}
