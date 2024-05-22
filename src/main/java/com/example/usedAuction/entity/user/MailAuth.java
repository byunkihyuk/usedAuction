package com.example.usedAuction.entity.user;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class MailAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_auth_id")
    private Long mailAuthId;

    private String mail;

    private String Number;
}
