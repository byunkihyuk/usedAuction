package com.example.usedAuction.service.see;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.example.usedAuction.dto.chat.ChattingRoomDto;
import com.example.usedAuction.entity.chat.ChattingRoom;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.chatting.ChattingRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseService {
    //private final List<Map<String, SseEmitter>> seeList = new ArrayList<>();
    private final Map<String, Map<String, SseEmitter>> auctionEmt = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> chatEmt = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 3600000L;
    private static final long RECONNECTION_TIMEOUT = 1000L;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity<SseEmitter> chatSubscribe(String userId) {

        if(chatEmt.containsKey(userId)){
            return ResponseEntity.ok( chatEmt.get(userId));
        }

        SseEmitter emitter = createChatSeeEmitter(userId);

        chatEmt.put(userId, emitter);

        try {
            emitter.send(sseChatEventBuilder("chat",userId,"Subscribed successfully.")); //503 방지를위한 더미데이터
        } catch (IOException e) {
            System.out.println("subscribe error : , "+ e.getMessage());
        }
        return ResponseEntity.ok(emitter);
    }

    private SseEmitter createChatSeeEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onTimeout(() -> {
            System.out.println("timed out : "+ userId);
            emitter.complete();
        });

        //에러 핸들러 등록
        emitter.onError(e -> {
            System.out.println("Error message : "+ e.getMessage());
            emitter.complete();
        });

        //SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            if (chatEmt.remove(userId) != null) {
                System.out.println("Remove userId : "+ userId);
            }
            System.out.println("disconnect usrId : "+ userId);
        });
        return emitter;
    }

    public void chatPublish(ChattingMessageDto chattingMessageDto) {

        ChattingRoom chattingRoomEntity = chattingRoomRepository.findById(chattingMessageDto.getRoomId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_CHATROOM));

        ChattingRoomDto roomDto = DataMapper.instance.chattingRoomEntityToDto(chattingRoomEntity);

        SseEmitter emitter1 = chatEmt.get(String.valueOf(roomDto.getSender()));
        SseEmitter emitter2 = chatEmt.get(String.valueOf(roomDto.getReceiver()));

        if(emitter1==null){
            emitter1 = createChatSeeEmitter(String.valueOf(roomDto.getSender()));
        }

        if(emitter2==null){
            emitter2 = createChatSeeEmitter(String.valueOf(roomDto.getReceiver()));
        }

        roomDto.setLastMessage(chattingMessageDto.getContent());
        roomDto.setMessageCreatedAt(chattingMessageDto.getCreatedAt());

        try {
            emitter1.send(sseChatEventBuilder("chat",String.valueOf(roomDto.getSender()),mapper.writeValueAsString(roomDto)));
        } catch (Exception e) {
            System.out.println("em1 에러");
            chatEmt.remove(String.valueOf(roomDto.getSender()));
        }

        try {
            emitter2.send(sseChatEventBuilder("chat",String.valueOf(roomDto.getReceiver()),mapper.writeValueAsString(roomDto)));
        } catch (Exception e) {
            System.out.println("em2 에러");
            chatEmt.remove(String.valueOf(roomDto.getReceiver()));
        }

    }

    private SseEmitter.SseEventBuilder sseChatEventBuilder(String name, String userId, String chattingRoomDto) {
        return SseEmitter.event()
                .name(name) //이벤트 명
                .id(userId) //이벤트 ID
                .data(chattingRoomDto) //전송 데이터
                .reconnectTime(RECONNECTION_TIMEOUT); // 재연결 대기시작
    }

    public ResponseEntity<SseEmitter> auctionTransactionSubscribe(String sessionId, String auctionTransactionId) {

        if(auctionEmt.containsKey(auctionTransactionId)){
            if(auctionEmt.get(auctionTransactionId).containsKey(sessionId)){
                return ResponseEntity.ok( auctionEmt.get(auctionTransactionId).get(sessionId));
            }
        }

        SseEmitter emitter = createAuctionSeeEmitter(auctionTransactionId);
        Map<String,SseEmitter> getEmt = auctionEmt.get(auctionTransactionId);
        if(getEmt==null){
            getEmt = new ConcurrentHashMap<>();
        }
        getEmt.put(sessionId,emitter);
        auctionEmt.put(auctionTransactionId, getEmt);

        try {
            emitter.send(sseAuctionEventBuilder("auctionBid",auctionTransactionId,"Subscribed successfully.")); //503 방지를위한 더미데이터
        } catch (IOException e) {
            System.out.println("sse 구독 에러 : , "+ e.getMessage());
        }
        return ResponseEntity.ok(emitter);
    }

    private SseEmitter createAuctionSeeEmitter(String auctionTransactionId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onTimeout(() -> {
            System.out.println("timed out : "+ auctionTransactionId);
            emitter.complete();
        });

        //에러 핸들러 등록
        emitter.onError(e -> {
            System.out.println("Error message : "+ e.getMessage());
            emitter.complete();
        });

        //SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            if (auctionEmt.remove(auctionTransactionId) != null) {
                System.out.println("Remove userId : "+ auctionTransactionId);
            }
            System.out.println("disconnect usrId : "+ auctionTransactionId);
        });
        return emitter;
    }

    public void auctionPublish(String auctionTransactionId,Integer price) {

        Map<String,SseEmitter> getEmt = auctionEmt.get(auctionTransactionId);
        Set<String> keyList = getEmt.keySet();
        System.out.println(keyList.size());
        for(String key : keyList){
            SseEmitter emitter = getEmt.get(key);
            if (emitter != null) {
                try {
                    emitter.send(sseAuctionEventBuilder("auctionBid",auctionTransactionId,String.valueOf(price)));
                } catch (IOException e) {
                    System.out.println("에러 : "+ e.getMessage());
                    System.out.println("삭제");
                    getEmt.remove(key);
                }
            }
        }
    }

    private SseEmitter.SseEventBuilder sseAuctionEventBuilder(String name, String userId, String message) {
        return SseEmitter.event()
                .name(name) //이벤트 명
                .id(userId) //이벤트 ID
                .data(message) //전송 데이터
                .reconnectTime(RECONNECTION_TIMEOUT); // 재연결 대기시작
    }


}
