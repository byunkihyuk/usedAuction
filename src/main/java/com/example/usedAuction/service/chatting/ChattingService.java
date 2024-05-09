package com.example.usedAuction.service.chatting;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.chat.ChattingMessageDto;
import com.example.usedAuction.dto.chat.ChattingRoomDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.chat.ChattingMessage;
import com.example.usedAuction.entity.chat.ChattingRoom;
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
    private final RedisPublisher redisPublisher;

    private static final String CHAT_ROOMS = "ROOM_INFO";
    private final RedisTemplate<String, ChattingMessageDto> redisTemplate;
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

        String username =SecurityUtil.getCurrentUsername().orElse("");
        User loginUser = userRepository.findByUsername(username)
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
                opsHashChatting.put(CHAT_ROOMS, String.valueOf(createRoom.getRoomId()), createRoom);
            }catch (Exception e){
                System.out.println("채팅방 생성 에러");
            }
        }

        enterRoom(String.valueOf(createRoom.getRoomId()));

        // 채팅하기를 누른 판매중인 상품 채팅방에 전송
        ChattingMessageDto chattingMessageDto = new ChattingMessageDto();
        chattingMessageDto.setSender(loginUser.getUserId());
        chattingMessageDto.setContent("http://usedauction.net:8080/general/"+generalTransactionDto.getGeneralTransactionId());
        chattingMessageDto.setRoomId(createRoom.getRoomId());
        // DTO 생성 및 메세지 저장
        sendChatting(chattingMessageDto);
        // redis에도 메시지 추가
        redisSaveMessage(chattingMessageDto);

        // 해당 채팅방 메시지 목록 리턴
        return getMessageList(chattingRoom.getRoomId(),0);
    }

    @Transactional(readOnly = true)
    public  ResponseEntity<Object>  getAllChattingRoom(){
        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
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

        ChattingMessage chattingMessage = DataMapper.instance.chattingMessageDtoToEntity(msg);
        chattingMessage.setSender(sender);
        
        try{
             ChattingMessage a = chattingMessageRepository.save(chattingMessage);
            System.out.println(a.getCreatedAt());
            // redis에도 채팅 내용 저장
            redisSaveMessage(msg);

        }catch (Exception e ){
            throw new ApiException(ErrorEnum.CHAT_MESSAGE_SEND_ERROR);
        }
    }

    public void redisSaveMessage(ChattingMessageDto msg){
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChattingMessageDto.class));
        redisTemplate.opsForList().rightPush(String.valueOf(msg.getRoomId()), msg);
        redisTemplate.expire(String.valueOf(msg.getRoomId()),10, TimeUnit.MINUTES);
    }

    public List<ChattingMessageDto> redisGetMessageList(String roomId, int start, int end){
        return redisTemplate.opsForList().range(roomId,start,end);
    }

    public List<ChattingMessageDto> getMessageList(Integer roomId,int start) {
        // 로그인 여부 확인
        String username = SecurityUtil.getCurrentUsername().orElse("");
        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        // redis hash에 방이 있는지 확인
        ChattingRoomDto chattingRoomDto = opsHashChatting.get(CHAT_ROOMS,String.valueOf(roomId));

        if(chattingRoomDto==null){
            // DB에 방이 있는지 확인
            ChattingRoom chattingRoom = chattingRepository.findById(roomId)
                    .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_CHATROOM));

            // redis hash에 채팅방 정보 저장
            opsHashChatting.put(CHAT_ROOMS, String.valueOf(chattingRoom.getRoomId()), DataMapper.instance.chattingRoomEntityToDto(chattingRoom));
        }

        // 채팅방 존재 시 topic에 방 추가
        enterRoom(String.valueOf(roomId));
        
        // redis에 채팅내역 검색
        List<ChattingMessageDto> messageDtoList = new ArrayList<>();// redisGetMessageList(String.valueOf(chattingRoomDto.getRoomId()),start,start+99);

        // redis에 채팅 내역 없으면 DB 검색
        // DB 페이징 추가
        if(messageDtoList!=null || messageDtoList.isEmpty()){
            Sort sort = Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(start/100,100,sort);
            messageDtoList = chattingMessageRepository.findAllByRoomId(DataMapper.instance.chattingRoomDtoToEntity(chattingRoomDto),pageable)
                    .stream().map(DataMapper.instance::chattingMessageEntityToDto)
                    .collect(Collectors.toList());
        }

        return messageDtoList;
    }
}
