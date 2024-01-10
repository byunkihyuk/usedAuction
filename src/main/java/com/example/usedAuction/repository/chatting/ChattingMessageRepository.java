package com.example.usedAuction.repository.chatting;

import com.example.usedAuction.entity.chat.ChattingMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChattingMessageRepository extends JpaRepository<ChattingMessage,Long> {
    List<ChattingMessage> findAllByRoomId(Integer roomId, Pageable pageable);
}
