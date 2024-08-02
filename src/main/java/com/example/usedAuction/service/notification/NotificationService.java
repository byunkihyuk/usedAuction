package com.example.usedAuction.service.notification;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.notification.NotificationDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.notification.Notification;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.notification.NotificationRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final Integer NOTIFICATION_COUNT = 20;

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllNotification(Integer start) {
        String loginUserName = SecurityUtil.getCurrentUsername().orElse("");

        if(loginUserName.isEmpty()){
            throw new ApiException(ErrorEnum.UNAUTHORIZED_ERROR);
        }

        User loginUser = userRepository.findByUsername(loginUserName)
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_USER));

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(start/NOTIFICATION_COUNT,NOTIFICATION_COUNT,sort);
        List<NotificationDto> list = notificationRepository.findByUserId(loginUser,pageable)
                .stream().map(DataMapper.instance::notificationEntityToDto).collect(Collectors.toList());

        Long newNotification = notificationRepository.countByUserIdAndReadornot(loginUser,false);

        Map<String,Object> map = new HashMap<>();
        map.put("notification",list);
        map.put("newNotification",newNotification);

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(map);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> readNotification(NotificationDto notificationDto) {
        String loginUserName = SecurityUtil.getCurrentUsername().orElse("");

        if(loginUserName.isEmpty()){
            throw new ApiException(ErrorEnum.UNAUTHORIZED_ERROR);
        }

        User loginUser = userRepository.findByUsername(loginUserName)
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_USER));

        Notification notification = notificationRepository.findById(notificationDto.getNotificationId())
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_NOTIFICATION));

        try{
            notification.setReadornot(true);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.NOTIFICATION_UPDATE_ERROR);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.notificationEntityToDto(notification));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> allReadNotification(Integer start) {
        String loginUserName = SecurityUtil.getCurrentUsername().orElse("");

        if(loginUserName.isEmpty()){
            throw new ApiException(ErrorEnum.UNAUTHORIZED_ERROR);
        }

        User loginUser = userRepository.findByUsername(loginUserName)
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_USER));

        try{
            notificationRepository.bulkReadAllNotification(loginUser);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.NOTIFICATION_UPDATE_ERROR);
        }

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(start/NOTIFICATION_COUNT,NOTIFICATION_COUNT,sort);
        List<NotificationDto> list = notificationRepository.findByUserId(loginUser,pageable)
                .stream().map(DataMapper.instance::notificationEntityToDto).collect(Collectors.toList());

        Long newNotification = notificationRepository.countByUserIdAndReadornot(loginUser,false);

        Map<String,Object> map = new HashMap<>();
        map.put("notification",list);
        map.put("newNotification",newNotification);

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(map);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public void saveNotification(User user, String message, String url){
        Notification notification = new Notification();
        notification.setUserId(user);
        notification.setMessage(message);
        notification.setUrl(url);

        try{
            notificationRepository.save(notification);
        }catch (Exception e) {
//            throw new ApiException(ErrorEnum.NOTIFICATION_ERROR);
        }
    }
}
