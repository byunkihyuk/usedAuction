package com.example.usedAuction.dto.chat;

import lombok.Data;

@Data
public class ChattingRoomDto {
    private Integer roomId;
    private Integer sender;
    private Integer receiver;
}
