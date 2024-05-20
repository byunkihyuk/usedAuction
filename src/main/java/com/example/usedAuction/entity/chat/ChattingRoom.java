package com.example.usedAuction.entity.chat;

import com.example.usedAuction.entity.user.User;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
public class ChattingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "sender")
    private User sender;

    private String senderNickname;

    @ManyToOne
    @JoinColumn(name = "receiver")
    private User receiver;

    private String receiverNickname;

    private String productThumbnail;

    private String lastMessage;

    private Timestamp messageCreatedAt;

    @OneToMany(mappedBy = "roomId",cascade = CascadeType.REMOVE)
    private List<ChattingMessage> chattingMessageList;
}
