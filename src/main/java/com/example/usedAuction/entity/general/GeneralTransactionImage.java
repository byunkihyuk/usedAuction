package com.example.usedAuction.entity.general;

import com.example.usedAuction.entity.TransactionImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class GeneralTransactionImage extends TransactionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_image_id")
    private Integer generalImageId;
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
    @JoinColumn(name = "general_transaction_id")
    private GeneralTransaction generalTransactionId;
}
