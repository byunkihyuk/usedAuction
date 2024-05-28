package com.example.usedAuction.dto.chat;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class ChattingMessageDto {
    private Long messageId;
    private String content;
    private Integer sender;
    private Integer receiver;
    private Integer roomId;
    private Timestamp createdAt;
}
