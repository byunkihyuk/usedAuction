package com.example.usedAuction.entity.general;

import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.transactionenum.TransactionModeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionPaymentEnum;
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
public class GeneralTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_transaction_id")
    private Integer generalTransactionId;

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

    @Column(name = "transaction_mode") // 거래 모드 (온라인(택배 거래), 직거래)
    @Enumerated(EnumType.STRING)
    private TransactionModeEnum transactionMode;

    private String location;

    @Column(name = "transaction_state") // 거래 상태
    @Enumerated(EnumType.STRING)
    private TransactionStateEnum transactionState;

    @Enumerated(EnumType.STRING) // 결제 방식 (온라인(웹머니), 직거래(계좌이체))
    private TransactionPaymentEnum payment;

    @Column(columnDefinition = "integer default 0")
    private Integer viewCount=0;

    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "generalTransactionId", cascade = CascadeType.REMOVE, orphanRemoval=true)
    private List<GeneralTransactionImage> generalTransactionImageList = new ArrayList<>();

    @OneToMany(mappedBy = "generalTransactionId", cascade = CascadeType.REFRESH)
    private List<PayInfo> PayInfoList = new ArrayList<>();

}
