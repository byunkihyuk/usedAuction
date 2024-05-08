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

//    // 출금
//    @PostMapping(value = "/withdrawal")
//    public ResponseEntity<Object> withdrawalMoney(@RequestBody PayInfoDto payInfoDto){
//        return paymentService.withdrawalMoney(payInfoDto) ;
//    }

    // 일반 거래글 결제 요청
    @PostMapping(value = "/general/payment")
    public ResponseEntity<Object> generalPaymentRequest(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPayment(payInfoDto);
    }

    @PostMapping(value = "/general/progress")
    public ResponseEntity<Object> generalPaymentProgress(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentProgress(payInfoDto);
    }

    @PutMapping(value = "/general/approve")
    public ResponseEntity<Object> generalPaymentApprove(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentApprove(payInfoDto);
    }

    @PutMapping(value = "/general/cancel")
    public ResponseEntity<Object> generalPaymentCancel(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentCancel(payInfoDto);
    }


}

