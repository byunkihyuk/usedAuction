package com.example.usedAuction.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class GeneralTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "general_transaction_id")
    private Integer generalTransactionId;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "transaction_mode",nullable = false)
    private String transactionMode;

    private String location;

    @Column(name = "transaction_state",nullable = false)
    private String transactionState;

    private String payment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @CreatedDate
    @Column(name = "created_at",nullable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
