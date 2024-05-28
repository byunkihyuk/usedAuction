package com.example.usedAuction.controller.sse;

import com.example.usedAuction.service.see.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class SseController {
    private final SseService sseService;

    @GetMapping(value = "/auction/{auctionTransactionId}/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> auctionBidSubscriber(@PathVariable String auctionTransactionId){
        return sseService.auctionTransactionSubscribe(auctionTransactionId);
    }

    @GetMapping(value = "/chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> chatSubscriber(@RequestParam(value = "user-id") String userId){
        return sseService.chatSubscribe(userId);
    }
}
