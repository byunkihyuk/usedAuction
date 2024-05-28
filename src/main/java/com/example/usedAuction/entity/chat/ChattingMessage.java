package com.example.usedAuction.entity.chat;

import com.example.usedAuction.config.JpaAuditingConfig;
import com.example.usedAuction.entity.user.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ChattingMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChattingRoom roomId;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(nullable = false,name = "sender")
    private User sender;

    @ManyToOne
    @JoinColumn(nullable = false,name = "receiver")
    private User receiver;

    @Column(name = "created_at")
    @CreatedDate
    private Timestamp createdAt;
}
