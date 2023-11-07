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

    @CreatedDate
    @Column(name ="created_at" ,nullable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name ="updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.REMOVE)
    private List<GeneralTransaction> generalTransactionList = new ArrayList<>();

}
