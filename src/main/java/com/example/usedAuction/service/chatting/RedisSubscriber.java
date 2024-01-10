package com.example.usedAuction.service.chatting;

import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messageSendingOperations;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            ChattingMessageDto chattingMessageDto = objectMapper.readValue(publishMessage,ChattingMessageDto.class);
            messageSendingOperations.convertAndSend("/queue/"+chattingMessageDto.getRoomId(),chattingMessageDto);
        } catch (JsonProcessingException e) {
            System.out.println("RedisSubscriber onMessage "+e.getMessage());
        }
    }
}
