    package com.example.usedAuction.config.chat;

import com.example.usedAuction.config.jwt.TokenProvider;
import com.example.usedAuction.dto.chat.ChattingRoomDto;
import com.example.usedAuction.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

    @RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    //private final TokenProvider jwtAuth;
    public final String CHAT_ROOMS = "ROOM_INFO";
    private HashOperations<String, String, ChattingRoomDto> opsHashChatting;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

//        if (accessor.getCommand() == StompCommand.CONNECT) {
//            String jwtToken = accessor.getFirstNativeHeader("token");
//            if(!jwtAuth.validateToken(jwtToken)){
//                System.out.println("유효하지 않은 토큰");
//                //throw new ApiException(ErrorEnum.UNAUTHORIZED_ERROR);
//                //log.info("Connect token = {}", jwtToken);
//            }
//            //log.info("Connect token = {}", jwtToken);
//
//        } else
//        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
//            String simpleDestination = (String) message.getHeaders().get("simpDestination");
//
//            if (simpleDestination == null) {
//                System.out.println("존재하지 않는 방입니다.");
//                //throw new ApiException(ErrorEnum.UNAUTHORIZED_ERROR);
//            }
//            //log.info("simpleDestination = {}", simpleDestination );
//            String roomId = getRoomId(simpleDestination);
//
//            String simpSessionId = (String) message.getHeaders().get("simpSessionId");
//            //log.info("roomId ={}  simpSessionId = {}" , roomId, simpSessionId);
//
//            // redis에 세션 id로 방정보 추가
//            redisRepository.mappingUserRoom( simpSessionId,roomId);
//
//            //String userEnterRoomId = redisRepository.getUserEnterRoomId(simpSessionId);
//            //log.info("구독성공 {}, {}", simpSessionId, userEnterRoomId);
//            //log.info("SUBSCRIBE {}, {}", simpSessionId, roomId);
//
//        }else
        if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
            String simpleDestination = (String) message.getHeaders().get("simpDestination");
            String roomId = getRoomId(simpleDestination);

            // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제
            opsHashChatting.delete(CHAT_ROOMS,roomId);
            //log.info("DISCONNECT {}, {}", sessionId, roomId);
        }
        return message;
    }

    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

}
