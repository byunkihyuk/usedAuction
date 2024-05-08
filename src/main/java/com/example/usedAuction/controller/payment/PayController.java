package com.example.usedAuction.controller.payment;

import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
public class PayController {
    private final PaymentService paymentService;

    // 내역 조회
    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getHistory(@PathVariable Integer userId){
        return paymentService.getHistory(userId);
    }

    // 충전
    @PostMapping(value = "/charging")
    public ResponseEntity<Object> chargingMoney(@RequestBody PayInfoDto payInfoDto){
        return paymentService.chargingMoney(payInfoDto);
    }


}

