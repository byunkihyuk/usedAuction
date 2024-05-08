package com.example.usedAuction.entity.payment;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.transactionenum.TransactionRequestStateEnum;
import com.example.usedAuction.entity.transactionenum.TransactionRequestTypeEnum;
import com.example.usedAuction.entity.transactionenum.UsedTransactionTypeEnum;
import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class PayInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_info_id")
    private Integer payInfoId;

    @JoinColumn(name = "seller")
    @ManyToOne
    private User seller;

    @JoinColumn(name = "buyer")
    @ManyToOne
    private User buyer;

    @Column(name = "transaction_request_type")
    @Enumerated(EnumType.STRING)
    private TransactionRequestTypeEnum transactionRequestType; // 충전 / 결제 / 출금 / 입금

    @Column(name = "transaction_request_state")
    @Enumerated(EnumType.STRING)
    private TransactionRequestStateEnum transactionRequestState; // 승인 / 대기 / 취소

    @Column(name = "transaction_money")
    private Integer transactionMoney = 0; // 샌더의 증감액

    @Column(name = "used_transaction_type")
    @Enumerated(EnumType.STRING)
    private UsedTransactionTypeEnum usedTransactionType; // 일반 거래글 거래 / 경매 거래글 입찰 / 입출금

    @JoinColumn(name = "general_transaction_id")
    @ManyToOne
    private GeneralTransaction generalTransactionId;

    @JoinColumn(name = "auction_transaction_id")
    @ManyToOne
    private AuctionTransaction auctionTransactionId;

    @Column(name = "transaction_time")
    @CreatedDate
    private Timestamp transactionTime;

    @Column(name = "transaction_update_time")
    @LastModifiedDate
    private Timestamp transactionUpdateTime;

}
