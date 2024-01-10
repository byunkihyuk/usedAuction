package com.example.usedAuction.service.chatting;

import com.example.usedAuction.dto.chat.ChattingMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, ChattingMessageDto chattingMessageDto){
        redisTemplate.convertAndSend(topic.getTopic(),chattingMessageDto);
    }
}
