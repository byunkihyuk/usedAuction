package com.example.usedAuction.controller.notification;

import com.example.usedAuction.dto.notification.NotificationDto;
import com.example.usedAuction.service.notification.NotificationService;
import com.example.usedAuction.service.see.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {
    private final NotificationService notificationService;
    private final SseService sseService;

    @Operation(summary = "알림 불러오기 API",description = "알림 정보를 Size 크기만큼 불러오기")
    @GetMapping("/notification")
    public ResponseEntity<Object> getAllNotification(@RequestParam(name = "start",required = true)Integer start){
        return notificationService.getAllNotification(start);
    }

    @Operation(summary = "선택한 알림 읽기",description = "선택한 알림 정보를 읽음 상태로 전환")
    @PutMapping("/notification-read")
    public ResponseEntity<Object> readNotification(@RequestBody NotificationDto notificationDto){
        return notificationService.readNotification(notificationDto);
    }

    @Operation(summary = "알림 전체 읽기",description = "새로 표시된 모든 알림 정보를 읽음 상태로 전환")
    @PutMapping("/notification-read-all")
    public ResponseEntity<Object> allReadNotification(@RequestParam(name = "start",required = true)Integer start){
        return notificationService.allReadNotification(start);
    }

    @Operation(hidden = true)
    @GetMapping(value = "/notification",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> chatSubscriber(@RequestParam(value = "nickname") String nickname){
        return sseService.notificationSubscribe(nickname);
    }
}
