package com.example.usedAuction.repository.chatting;

import com.example.usedAuction.entity.chat.ChattingMessage;
import com.example.usedAuction.entity.chat.ChattingRoom;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom,Integer> {
    List<ChattingRoom> findAllBySenderOrReceiver(User sender,User receiver);

    ChattingRoom findBySenderAndReceiver(User loginUser, User receiver);

    List<ChattingMessage> findAllByRoomId(ChattingRoom chattingRoom);
}
