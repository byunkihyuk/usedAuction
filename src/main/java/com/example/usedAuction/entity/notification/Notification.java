package com.example.usedAuction.entity.notification;

import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    private String message;

    private String url;

    @CreatedDate
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(columnDefinition = "TINYINT",name = "read_or_not")
    private boolean readornot;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;



}
