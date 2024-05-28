package com.example.usedAuction.controller.chat;

import com.example.usedAuction.dto.chat.ChattingMessageForm;
import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.service.chatting.ChattingService;
import com.example.usedAuction.service.chatting.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChattingService chattingService;

    // redis
    private final RedisPublisher redisPublisher;

    // 메시지 전송
    @MessageMapping("/users")
    public void greeting(ChattingMessageDto msg) throws Exception {
        //simpMessagingTemplate.convertAndSend("/queue/"+chattingForm.getRoomId(),chattingForm);
        // 메시지 보내기
        redisPublisher.publish(chattingService.getTopic(String.valueOf(msg.getRoomId())),msg);
        // 메시지 DB 저장
        chattingService.sendChatting(msg);
    }

    // 채팅방 목록
    @GetMapping("/chat")
    public ResponseEntity<Object> getAllChatting(){
        return chattingService.getAllChattingRoom();
    }

    // 채팅방 생성 - 채팅하기
    @PostMapping("/chat")
    public ResponseEntity<Object> postChatting(@RequestBody GeneralTransactionDto generalTransactionDto){
        List<ChattingMessageDto> messageDtoList = chattingService.postChattingRoom(generalTransactionDto);
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(messageDtoList);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    
    // 채팅방 입장 - 채팅 내용 가져오기
    @GetMapping("/chat/{roomId}")
    public ResponseEntity<Object> getChatting(@PathVariable Integer roomId,@RequestParam(value = "start", required = false, defaultValue = "0") int start){
        System.out.println(start/50);
        List<ChattingMessageDto> messageDtoList = chattingService.getMessageList(roomId,start);

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(messageDtoList);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 채팅방 입장 - 상대방 닉네임,번호 가져오기
    @GetMapping("/chat-receiver/{roomId}")
    public ResponseEntity<Object> getChattingReceiver(@PathVariable Integer roomId){
        return ResponseEntity.status(HttpStatus.OK).body(chattingService.getChattingReceiver(roomId));
    }
}
