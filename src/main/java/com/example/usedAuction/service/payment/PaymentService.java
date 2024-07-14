package com.example.usedAuction.service.payment;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.notification.NotificationDto;
import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.transactionenum.*;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionBidRepository;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.notification.NotificationRepository;
import com.example.usedAuction.repository.payment.PayInfoRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.service.notification.NotificationService;
import com.example.usedAuction.service.see.SseService;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PayInfoRepository payInfoRepository;
    private final UserRepository userRepository;
    private final GeneralTransactionRepository generalTransactionRepository;
    private final AuctionTransactionRepository auctionTransactionRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getHistory(Integer userId) {
        User loingUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!Objects.equals(loingUser.getUserId(), userId)){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }
        List<PayInfoDto> payInfoDtoList = payInfoRepository.findAllBySellerOrBuyerOrderByTransactionUpdateTime(loingUser,loingUser).stream()
                .map(DataMapper.instance::payInfoEntityToDto).collect(Collectors.toList());

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(payInfoDtoList);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> chargingMoney(PayInfoDto payInfoDto) {

        // 현재 로그인한 유저와 userId가 맞는지 확인
        User loingUser = userRepository.findByUsernamePessimisticLock(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!Objects.equals(loingUser.getUserId(), payInfoDto.getSeller())){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }
        // 충전 금액 저장
        //DataMapper.instance.payInfoDtoToEntity(payInfoDto);
        /// DataMapper 사용시 general,auction Transaction 생성해서 오류 발생
        PayInfo savePay = new PayInfo();
        savePay.setSeller(loingUser);
        savePay.setSellerNickname(loingUser.getNickname());
        savePay.setTransactionMoney(payInfoDto.getTransactionMoney());
        savePay.setTransactionRequestType(payInfoDto.getTransactionRequestType());
        savePay.setTransactionRequestState(payInfoDto.getTransactionRequestState());
        savePay.setUsedTransactionType(payInfoDto.getUsedTransactionType());

        PayInfo payInfoEntity = payInfoRepository.save(savePay);

        // 사용자 금액 변경
        try {
            loingUser.setMoney(loingUser.getMoney() + payInfoEntity.getTransactionMoney());
            payInfoEntity.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(ErrorEnum.CHARGING_FAIL);
        }

        PayInfoDto resultDto = DataMapper.instance.payInfoEntityToDto(payInfoEntity);

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(resultDto);

        return ResponseEntity.status(HttpStatus.OK).body(resultDto);
    }

//    @Transactional
//    public ResponseEntity<Object> withdrawalMoney(PayInfoDto payInfoDto) {
//        // 현재 로그인한 유저와 userId가 맞는지 확인
//        User loingUser = userRepository.findByUsernamePessimisticLock(SecurityUtil.getCurrentUsername().orElse(""))
//                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
//
//        if(!Objects.equals(loingUser.getUserId(), payInfoDto.getSeller())){
//            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
//        }
//
//        // 출금 금액이 잔금보다 많은지 확인
//        if(loingUser.getMoney()+payInfoDto.getTransactionMoney() < 0){
//            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
//        }
//
//        // 출금
//        payInfoDto.setSeller(loingUser.getUserId());
//
//        try{
//            // 사용자 금액 변경
//            loingUser.setMoney(loingUser.getMoney()+payInfoDto.getTransactionMoney());
//            payInfoDto.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
//        }catch (Exception e){
//            throw new ApiException(ErrorEnum.WITHDRAWAL_FAIL);
//        }
//
//        PayInfo savePay = new PayInfo();
//        savePay.setSeller(loingUser);
//        savePay.setTransactionMoney(payInfoDto.getTransactionMoney());
//        savePay.setTransactionRequestType(payInfoDto.getTransactionRequestType());
//        savePay.setTransactionRequestState(payInfoDto.getTransactionRequestState());
//        savePay.setUsedTransactionType(payInfoDto.getUsedTransactionType());
//
//        PayInfo payInfo = payInfoRepository.save(savePay);
//
//        ResponseResult<Object> result = new ResponseResult<>();
//        result.setStatus("success");
//        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));
//
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }

    @Transactional
    public ResponseEntity<Object> generalPayment(PayInfoDto payInfoDto) {
        // 글이 존재하는지
        GeneralTransaction generalTransaction = generalTransactionRepository.findById(payInfoDto.getGeneralTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_GENERAL_TRANSACTION));
        // seller와 buyer가 존재하는지
        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User loginUser = userRepository.findByUsernamePessimisticLock(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));

        // 요청한 내역이 있는지 확인
        PayInfo existPayInfo = payInfoRepository.findByGeneralTransactionIdAndBuyer(generalTransaction, loginUser)
                .orElse(null);

        if(existPayInfo!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResult<>("fail",DataMapper.instance.payInfoEntityToDto(existPayInfo)));
        }

        // buyer의 잔금이 결제금액보다 많은지 확인
        if(loginUser.getMoney() < generalTransaction.getPrice()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        PayInfo savePay = new PayInfo();
        savePay.setSeller(seller);
        savePay.setSellerNickname(seller.getNickname());
        savePay.setBuyer(loginUser);
        savePay.setBuyerNickname(loginUser.getNickname());
        savePay.setTransactionMoney(generalTransaction.getPrice());
        savePay.setTransactionRequestType(TransactionRequestTypeEnum.PAYMENT);
        savePay.setTransactionRequestState(TransactionRequestStateEnum.WAIT);
        savePay.setUsedTransactionType(UsedTransactionTypeEnum.GENERAL_TRANSACTION);
        savePay.setGeneralTransactionId(generalTransaction);

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto.setMessage(loginUser.getNickname() + "님으로부터 " + generalTransaction.getTitle() + "글의 구매요청이 왔습니다.");
        notificationDto.setUserId(seller.getUserId());

        try {
            // 머니 결제 요청
            payInfoRepository.save(savePay);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto));
            sseService.notificationPublish(notificationDto);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_PAYMENT_FAIL);
        }
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(savePay));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> generalPaymentUpdate(PayInfoDto payInfoDto) {
        // 글이 존재하는지
        GeneralTransaction generalTransaction = generalTransactionRepository.findById(payInfoDto.getGeneralTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_GENERAL_TRANSACTION));

        if(!generalTransaction.getTransactionState().equals(TransactionStateEnum.SALE)){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","거래 완료 또는 거래 진행중 상품입니다."));
        }

        // seller와 buyer가 존재하는지
        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User loginUser = userRepository.findByUsernamePessimisticLock(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.UNAUTHORIZED_ERROR));

        // 요청한 내역이 있는지 확인
        PayInfo payInfo = payInfoRepository.findByGeneralTransactionIdAndBuyer(generalTransaction, loginUser)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        payInfo.setTransactionRequestState(TransactionRequestStateEnum.WAIT);

        // buyer의 잔금이 결제금액보다 많은지 확인
        if(loginUser.getMoney() < generalTransaction.getPrice()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> generalPaymentProgress(PayInfoDto payInfoDto) {
        // 글이 존재하는지
        GeneralTransaction generalTransaction = generalTransactionRepository.findById(payInfoDto.getGeneralTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_GENERAL_TRANSACTION));
        // seller와 buyer 존재하는지
        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        // 현재 로그인한 사용자가 seller인지
        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        // 결제 정보가 있는지
        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        NotificationDto notificationDto1 = new NotificationDto();
        notificationDto1.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto1.setMessage(generalTransaction.getTitle() + "글이 거래중으로 변경되었습니다.");
        notificationDto1.setUserId(seller.getUserId());

        NotificationDto notificationDto2 = new NotificationDto();
        notificationDto2.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto2.setMessage(generalTransaction.getTitle() + "글이 거래중으로 변경되었습니다.");
        notificationDto2.setUserId(loginUser.getUserId());

        try {
            // 거래 글 예약중으로 변경
            generalTransaction.setTransactionState(TransactionStateEnum.PROGRESS);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.PROGRESS);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto1));
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto2));
            sseService.notificationPublish(notificationDto1);
            sseService.notificationPublish(notificationDto2);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_APPROVE_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> generalPaymentApprove(PayInfoDto payInfoDto) {

        // 글이 존재하는지
        GeneralTransaction generalTransaction = generalTransactionRepository.findById(payInfoDto.getGeneralTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_GENERAL_TRANSACTION));

        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User loginUser = userRepository.findByUsernamePessimisticLock(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!(Objects.equals(loginUser.getUserId(), payInfoDto.getBuyer()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        if(loginUser.getMoney() < payInfo.getTransactionMoney()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        NotificationDto notificationDto1 = new NotificationDto();
        notificationDto1.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto1.setMessage(generalTransaction.getTitle() + "글의 거래가 완료되었습니다.");
        notificationDto1.setUserId(seller.getUserId());

        NotificationDto notificationDto2 = new NotificationDto();
        notificationDto2.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto2.setMessage(generalTransaction.getTitle() + "글의 거래가 완료되었습니다.");
        notificationDto2.setUserId(loginUser.getUserId());

        try {
            // 판매자 잔액 수정
            seller.setMoney(seller.getMoney() + payInfo.getTransactionMoney());
            // 구매자 잔액 수정
            loginUser.setMoney(loginUser.getMoney() - payInfo.getTransactionMoney());
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
            generalTransaction.setTransactionState(TransactionStateEnum.COMPLETE);
            generalTransaction.setBuyer(loginUser);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto1));
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto2));
            sseService.notificationPublish(notificationDto1);
            sseService.notificationPublish(notificationDto2);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_APPROVE_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> generalPaymentCancel(PayInfoDto payInfoDto) {
        // 결제 정보가 있기 때문에 ID로 DB에서 조회
        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        if(payInfo.getTransactionRequestState().equals(TransactionRequestStateEnum.APPROVE)){
            throw new ApiException(ErrorEnum.PAYMENT_COMPLETED_CANCEL_FAIL);
        }

        // 글이 존재하는지
        GeneralTransaction generalTransaction = generalTransactionRepository.findById(payInfoDto.getGeneralTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_GENERAL_TRANSACTION));
        // seller와 buyer가 존재하는지
        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User buyer = userRepository.findByUserIdPessimisticLock(payInfoDto.getBuyer())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));
        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()) || Objects.equals(loginUser.getUserId(), buyer.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        NotificationDto notificationDto1 = new NotificationDto();
        notificationDto1.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto1.setMessage(generalTransaction.getTitle() + "글의 거래가 취소되었습니다.");
        notificationDto1.setUserId(seller.getUserId());

        NotificationDto notificationDto2 = new NotificationDto();
        notificationDto2.setUrl(SseService.domain+"/general/"+generalTransaction.getGeneralTransactionId());
        notificationDto2.setMessage(generalTransaction.getTitle() + "글의 거래가 취소되었습니다.");
        notificationDto2.setUserId(loginUser.getUserId());

        try {
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.CANCEL);
            generalTransaction.setTransactionState(TransactionStateEnum.SALE);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto1));
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto2));
            sseService.notificationPublish(notificationDto1);
            sseService.notificationPublish(notificationDto2);
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.PAYMENT_CANCEL_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> auctionPaymentRequest(PayInfoDto payInfoDto, Integer auctionBidId) {
        // 글이 존재하는지
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findById(payInfoDto.getAuctionTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        // 입찰 정보가 있는지
        AuctionBid auctionBid = auctionBidRepository.findById(auctionBidId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_BID));

        // 판매자 구매자가 있는 유저인지 확인
        User seller = userRepository.findByUserId(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User bidder = userRepository.findByUserId(payInfoDto.getBuyer())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

//        if(!(Objects.equals(bidder.getUserId(), payInfoDto.getBuyer()))){
//            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
//        }

        payInfoDto.setAuctionTransactionId(payInfoDto.getAuctionTransactionId());
        PayInfo savePay = new PayInfo();
        savePay.setSeller(seller);
        savePay.setSellerNickname(seller.getNickname());
        savePay.setBuyer(bidder);
        savePay.setBuyerNickname(bidder.getNickname());
        savePay.setTransactionMoney(auctionBid.getPrice());
        savePay.setTransactionRequestType(TransactionRequestTypeEnum.PAYMENT);
        savePay.setTransactionRequestState(TransactionRequestStateEnum.PROGRESS);
        savePay.setUsedTransactionType(UsedTransactionTypeEnum.AUCTION_TRANSACTION);
        savePay.setAuctionTransactionId(auctionTransaction);

        PayInfo payInfo = null;

        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setUrl(SseService.domain+"/auction/"+auctionTransaction.getAuctionTransactionId());
        notificationDto.setMessage(auctionTransaction.getTitle() + "글의 거래가 진행중으로 변경되었습니다.");
        notificationDto.setUserId(bidder.getUserId());

        try {
            payInfo = payInfoRepository.save(savePay);
            auctionBid.setAuctionBidState(AuctionBidStateEnum.WAIT);
            auctionTransaction.setTransactionState(TransactionStateEnum.PROGRESS);
            auctionTransaction.setBuyer(bidder);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto));
            sseService.notificationPublish(notificationDto);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.AUCTION_TRANSACTION_PAYMENT_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> auctionPaymentApprove(PayInfoDto payInfoDto, Integer auctionBidId) {

        AuctionTransaction auctionTransaction = auctionTransactionRepository.findById(payInfoDto.getAuctionTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));
        // 입찰 정보가 있는지
        AuctionBid auctionBid = auctionBidRepository.findById(auctionBidId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_BID));

        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User buyer = userRepository.findByUserIdPessimisticLock(payInfoDto.getBuyer())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!( Objects.equals(loginUser.getUserId(), buyer.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        PayInfo payInfo = payInfoRepository.findByAuctionTransactionIdAndBuyer(auctionTransaction,loginUser)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        if(buyer.getMoney() < payInfo.getTransactionMoney()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        NotificationDto notificationDto1 = new NotificationDto();
        notificationDto1.setUrl(SseService.domain+"/auction/"+auctionTransaction.getAuctionTransactionId());
        notificationDto1.setMessage(auctionTransaction.getTitle() + "글의 거래가 완료되었습니다.");
        notificationDto1.setUserId(seller.getUserId());

        NotificationDto notificationDto2 = new NotificationDto();
        notificationDto2.setUrl(SseService.domain+"/auction/"+auctionTransaction.getAuctionTransactionId());
        notificationDto2.setMessage(auctionTransaction.getTitle() + "글의 거래가 완료되었습니다.");
        notificationDto2.setUserId(loginUser.getUserId());

        // sender 잔액 수정
        try {
            seller.setMoney(seller.getMoney() + payInfo.getTransactionMoney());
            buyer.setMoney(buyer.getMoney() - payInfo.getTransactionMoney());
            auctionBid.setAuctionBidState(AuctionBidStateEnum.APPROVE);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
            auctionTransaction.setTransactionState(TransactionStateEnum.COMPLETE);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto1));
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto2));
            sseService.notificationPublish(notificationDto1);
            sseService.notificationPublish(notificationDto2);
        }catch ( Exception e ){
            throw new ApiException(ErrorEnum.AUCTION_TRANSACTION_APPROVE_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> auctionPaymentCancel(PayInfoDto payInfoDto, Integer auctionBidId) {
        AuctionTransaction auctionTransaction = auctionTransactionRepository.findById(payInfoDto.getAuctionTransactionId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_AUCTION_TRANSACTION));

        // 입찰 정보가 있는지
        AuctionBid auctionBid = auctionBidRepository.findById(auctionBidId)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_BID));

        User seller = userRepository.findByUserId(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User buyer = userRepository.findByUserId(payInfoDto.getBuyer())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()) || Objects.equals(loginUser.getUserId(), buyer.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        PayInfo payInfo = payInfoRepository.findByAuctionTransactionIdAndBuyer(auctionTransaction,buyer)
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        if(payInfo.getTransactionRequestState().equals(TransactionRequestStateEnum.APPROVE)){
            throw new ApiException(ErrorEnum.PAYMENT_COMPLETED_CANCEL_FAIL);
        }

        NotificationDto notificationDto1 = new NotificationDto();
        notificationDto1.setUrl(SseService.domain+"/auction/"+auctionTransaction.getAuctionTransactionId());
        notificationDto1.setMessage(auctionTransaction.getTitle() + "글의 거래가 취소되었습니다.");
        notificationDto1.setUserId(seller.getUserId());

        NotificationDto notificationDto2 = new NotificationDto();
        notificationDto2.setUrl(SseService.domain+"/auction/"+auctionTransaction.getAuctionTransactionId());
        notificationDto2.setMessage(auctionTransaction.getTitle() + "글의 거래가 취소되었습니다.");
        notificationDto2.setUserId(loginUser.getUserId());

        try {
            auctionBid.setAuctionBidState(AuctionBidStateEnum.CANCEL);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.CANCEL);
            auctionTransaction.setTransactionState(TransactionStateEnum.SALE);
            auctionTransaction.setBuyer(null);
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto1));
            notificationRepository.save(DataMapper.instance.notificationDtoToEntity(notificationDto2));
            sseService.notificationPublish(notificationDto1);
            sseService.notificationPublish(notificationDto2);
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.PAYMENT_CANCEL_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

