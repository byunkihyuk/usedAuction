package com.example.usedAuction.entity;

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
    @JoinColumn(name = "user_id")
    private User userId;

    private String thumbnail;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "transaction_mode",nullable = false) // 거래 모드 (직거래, 택배 거래)
    private String transactionMode;

    private String location;

    @Column(name = "transaction_state",nullable = false) // 거래 상태
    private String transactionState;

    private String payment;

    @CreatedDate
    @Column(name = "created_at",nullable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "generalTransactionId", cascade = CascadeType.REMOVE, orphanRemoval=true)
    private List<GeneralTransactionImage> generalTransactionImageList = new ArrayList<>();
}
