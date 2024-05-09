package com.example.usedAuction.dto.chat;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChattingRoomDto implements Serializable {

    private Integer roomId;
    private Integer sender;
    private Integer receiver;
}
