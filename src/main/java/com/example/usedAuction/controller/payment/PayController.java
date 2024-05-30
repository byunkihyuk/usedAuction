package com.example.usedAuction.controller.payment;

import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay")
@Tag(name = "결제",description = "PaymentController")
public class PayController {
    private final PaymentService paymentService;

    // 내역 조회
    @Operation(summary = "사용자의 머니 거래내역 조회 API (JWT 토큰 필요)",description = "머니 거래 내역 전체 조회")
    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getHistory(@PathVariable Integer userId){
        return paymentService.getHistory(userId);
    }

    // 충전
    @Operation(summary = "머니 충전 API (JWT 토큰 필요)",description = "머니 충전")
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
    @Operation(summary = "중고 거래 글 구매 요청 API (JWT 토큰 필요)",description = "중고 거래 글 구매 요청 전송")
    @PostMapping(value = "/general/payment")
    public ResponseEntity<Object> generalPaymentRequest(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPayment(payInfoDto);
    }

    @Operation(summary = "중고 거래 글 구매 취소된 요청 저개래 요청 API (JWT 토큰 필요)",description = "중고 거래 글 구매 취소된 요청 저개래 요청")
    @PutMapping(value = "/general/payment")
    public ResponseEntity<Object> generalPaymentUpdate(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentUpdate(payInfoDto);
    }

    @Operation(summary = "중고 거래 글 구매 요청 거래 진행중 상태 변환 API (JWT 토큰 필요)",description = "중고 거래 글 구매 요청 거래 진행중 상태 변환")
    @PutMapping(value = "/general/progress")
    public ResponseEntity<Object> generalPaymentProgress(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentProgress(payInfoDto);
    }

    @Operation(summary = "중고 거래 글 구매 요청 거래 완료 상태 변환 API (JWT 토큰 필요)",description = "중고 거래 글 구매 요청 거래 완료 상태 변환")
    @PutMapping(value = "/general/approve")
    public ResponseEntity<Object> generalPaymentApprove(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentApprove(payInfoDto);
    }

    @Operation(summary = "중고 거래 글 구매 요청 취소 API (JWT 토큰 필요)",description = "중고 거래 글 구매 요청 취소")
    @PutMapping(value = "/general/cancel")
    public ResponseEntity<Object> generalPaymentCancel(@RequestBody PayInfoDto payInfoDto){
        return paymentService.generalPaymentCancel(payInfoDto);
    }

    // 경매 거래 글 입찰 거래
    @Operation(summary = "경매 거래 글 거래 진행 API (JWT 토큰 필요)",description = "경매 거래 글 거래 진행")
    @PostMapping(value = "/auction/bid/{auctionBidId}")
    public ResponseEntity<Object> auctionPaymentRequest(@RequestBody PayInfoDto payInfoDto, @PathVariable Integer auctionBidId){
        return paymentService.auctionPaymentRequest(payInfoDto,auctionBidId);
    }

    // 경매 거래 글 승인
    @Operation(summary = "경매 거래 글 거래 승인 API (JWT 토큰 필요)",description = "경매 거래 글 거래 승인(구매확정)")
    @PutMapping(value = "/auction/bid/{auctionBidId}/approve")
    public ResponseEntity<Object> auctionPaymentApprove(@RequestBody PayInfoDto payInfoDto, @PathVariable Integer auctionBidId){
        return paymentService.auctionPaymentApprove(payInfoDto,auctionBidId);
    }

    // 경매 거래 글 취소
    @Operation(summary = "경매 거래 글 거래 진행중 취소 API (JWT 토큰 필요)",description = "경매 거래 글 거래 진행중 취소")
    @PutMapping(value = "/auction/bid/{auctionBidId}/cancel")
    public ResponseEntity<Object> auctionPaymentCancel(@RequestBody PayInfoDto payInfoDto, @PathVariable Integer auctionBidId){
        return paymentService.auctionPaymentCancel(payInfoDto,auctionBidId);
    }

}

