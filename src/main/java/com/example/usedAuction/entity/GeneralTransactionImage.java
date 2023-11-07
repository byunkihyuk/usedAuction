package com.example.usedAuction.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class GeneralTransactionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_image_id")
    private Integer generalImageId;
    @Column(name = "image_seq")
    private Integer imageSeq;
    @Column(name = "origin_url")
    private String originUrl;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "upload_url")
    private String uploadUrl;
    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "general_transaction_id")
    private GeneralTransaction generalTransactionId;
}
