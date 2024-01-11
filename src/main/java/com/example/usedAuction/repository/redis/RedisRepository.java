package com.example.usedAuction.repository.redis;

import com.example.usedAuction.dto.chat.ChattingRoomDto;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class RedisRepository {
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChattingRoomDto> userRoomMap;

//    public void mappingUserRoom(String sessionId, String roomId){
//        userRoomMap.put(CHAT_ROOMS, sessionId, roomId);
//    }

//    public ChattingRoomDto getUserEnterRoomId(String roomId) {
//        return userRoomMap.get(CHAT_ROOMS, roomId);
//    }
//
//    public void removeUserEnterInfo(String sessionId) {
//        userRoomMap.delete(CHAT_ROOMS, sessionId);
//    }


}
