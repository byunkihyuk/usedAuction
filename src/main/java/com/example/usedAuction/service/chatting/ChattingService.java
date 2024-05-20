package com.example.usedAuction.service.chatting;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.example.usedAuction.dto.chat.ChattingRoomDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.chat.ChattingMessage;
import com.example.usedAuction.entity.chat.ChattingRoom;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.chatting.ChattingMessageRepository;
import com.example.usedAuction.repository.chatting.ChattingRoomRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChattingService {
    private final ChattingRoomRepository chattingRepository;
    private final ChattingMessageRepository chattingMessageRepository;
    private final UserRepository userRepository;
    private final GeneralTransactionRepository generalTransactionRepository;

    // redis
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;

    private static final String CHAT_ROOMS = "ROOM_INFO";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChattingRoomDto> opsHashChatting;
    private Map<String, ChannelTopic> topics;

    // 의존성 주입이 완료된 후 실행되어야하는 method에 사용
    // 생성자보다 늦게 호출
    // bean이 초기화 되는것과 동시에 의존성을 확인할 수 있다
    @PostConstruct
    private void init() {
        opsHashChatting = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    public void enterRoom(String roomId){
        ChannelTopic topic = topics.get(roomId);
        if(topic==null){
            topic = new ChannelTopic(roomId);
        }
        redisMessageListener.addMessageListener(redisSubscriber,topic);
        topics.put(roomId,topic);
    }


    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }


    @Transactional
    public List<ChattingMessageDto> postChattingRoom(GeneralTransactionDto generalTransactionDto){
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User receiver = userRepository.findById(generalTransactionDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));


        // DB에 채팅방이 있는지 검색
        ChattingRoom chattingRoom = chattingRepository.findBySenderAndReceiver(loginUser,receiver);
        ChattingRoomDto createRoom = null;

        // 채팅방이 없다면 채팅방 생성
        if(chattingRoom==null){
            ChattingRoomDto createRoomDto = new ChattingRoomDto();
            createRoomDto.setSender(loginUser.getUserId());
            createRoomDto.setReceiver(receiver.getUserId());
            chattingRoom = DataMapper.instance.chattingRoomDtoToEntity(createRoomDto);
            try {
                createRoom = DataMapper.instance.chattingRoomEntityToDto(chattingRepository.save(chattingRoom));
                // redis hash에 채팅방 정보 저장
                ////opsHashChatting.put(CHAT_ROOMS, String.valueOf(createRoom.getRoomId()), createRoom);
            }catch (Exception e){
                throw new ApiException(ErrorEnum.CHAT_ROOM_CREATE_ERROR);
            }
        }

        // 채팅하기를 누른 판매중인 상품 채팅방에 전송
        ChattingMessageDto chattingMessageDto = new ChattingMessageDto();
        chattingMessageDto.setSender(loginUser.getUserId());
        chattingMessageDto.setContent("https://usedauction.net/general/"+generalTransactionDto.getGeneralTransactionId());

        // DTO 생성 및 메세지 저장
        sendChatting(chattingMessageDto);
        // redis에도 메시지 추가
        redisSaveMessage(chattingMessageDto);

        // 해당 채팅방 메시지 목록 리턴
        return getMessageList(chattingRoom.getRoomId(),0);
    }

    @Transactional(readOnly = true)
    public  ResponseEntity<Object>  getAllChattingRoom(){
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        List<ChattingRoomDto> chattingList = chattingRepository.findAllBySenderOrReceiver(loginUser,loginUser)
                .stream().map(DataMapper.instance::chattingRoomEntityToDto)
                .collect(Collectors.toList());

        ResponseResult<Object> result = new ResponseResult<>();

        result.setStatus("success");
        result.setData(chattingList);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @Transactional
    public void sendChatting(ChattingMessageDto msg) {
        User sender = userRepository.findById(msg.getSender())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        // 메시지
        ChattingMessage chattingMessage = DataMapper.instance.chattingMessageDtoToEntity(msg);
        chattingMessage.setSender(sender);

        try{
            ChattingMessage saveSendMessage = chattingMessageRepository.save(chattingMessage);

            // redis에도 채팅 내용 저장
            redisSaveMessage(msg);
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.CHAT_MESSAGE_SEND_ERROR);
        }
    }

    public void redisSaveMessage(ChattingMessageDto msg){
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChattingMessageDto.class));
        redisTemplate.opsForList().rightPush(String.valueOf(msg.getRoomId()), msg);
        redisTemplate.expire(String.valueOf(msg.getRoomId()),5, TimeUnit.MINUTES);
    }

    public List<ChattingMessageDto> redisGetMessageList(String roomId, int start, int end){
        return redisTemplate.opsForList().range(roomId,start,end);
    }

    public List<ChattingMessageDto> getMessageList(Integer roomId,int start) {
        // 로그인 여부 확인
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        ChattingRoomDto chattingRoomDto = null;

        // redis hash에 방이 있는지 확인
        try {
            chattingRoomDto = opsHashChatting.get(CHAT_ROOMS, String.valueOf(roomId));
        }catch (Exception e){
            System.out.println("레디스 채팅방 없음");
        }

        if(chattingRoomDto==null){
            // redis에 방이 없다면 DB에 방이 있는지 확인
            ChattingRoom chattingRoom = chattingRepository.findById(roomId)
                    .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_CHATROOM));

            // redis hash에 채팅방 정보 저장
            opsHashChatting.put(CHAT_ROOMS, String.valueOf(chattingRoom.getRoomId()), DataMapper.instance.chattingRoomEntityToDto(chattingRoom));
        }

        // 채팅방 존재 시 topic에 방 추가
        enterRoom(String.valueOf(roomId));
        
        // redis에 채팅내역 검색
        List<ChattingMessageDto> messageDtoList =   redisGetMessageList(String.valueOf(chattingRoomDto.getRoomId()),start,start+99);

        // redis에 채팅 내역 없으면 DB 검색
        // DB 페이징 추가
        if(messageDtoList!=null || messageDtoList.isEmpty()){
            Sort sort = Sort.by("createdAt").ascending();
            Pageable pageable = PageRequest.of(start/100,100,sort);
            messageDtoList = chattingMessageRepository.findAllByRoomId(DataMapper.instance.chattingRoomDtoToEntity(chattingRoomDto),pageable)
                    .stream().map(DataMapper.instance::chattingMessageEntityToDto)
                    .collect(Collectors.toList());
        }

        return messageDtoList;
    }

    public Object getChattingReceiver(Integer roomId) {
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));
        ChattingRoom chattingRoom = chattingRepository.findById(roomId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_CHATROOM));

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> data = new HashMap<>();

        if(chattingRoom.getSender().getUserId().equals(loginUser.getUserId())){
            map.put("status","success");
            data.put("receiverId",chattingRoom.getReceiver().getUserId());
            data.put("nickname",chattingRoom.getReceiver().getNickname());
            map.put("data",data);
        }else if(chattingRoom.getReceiver().getUserId().equals(loginUser.getUserId())){
            map.put("status","success");
            data.put("receiverId",chattingRoom.getSender().getUserId());
            data.put("nickname",chattingRoom.getSender().getNickname());
            map.put("data",data);
        }else{
            map.put("status","fail");
            map.put("message","fail");
        }

        return map;
    }
}

