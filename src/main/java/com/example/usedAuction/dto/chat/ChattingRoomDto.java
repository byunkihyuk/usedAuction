package com.example.usedAuction.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ChattingRoomDto implements Serializable {

    private Integer roomId;
    private Integer sender;
    private String senderNickname;
    private Integer receiver;
    private String receiverNickname;
    private String productThumbnail;
    private String lastMessage;
    private Timestamp messageCreatedAt;
}
