package com.example.usedAuction.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class ChattingMessageForm {
    private Integer roomId;
    private String content;
    private Integer sender;
    //private Integer receiver;
    private Timestamp createdAt;
}
