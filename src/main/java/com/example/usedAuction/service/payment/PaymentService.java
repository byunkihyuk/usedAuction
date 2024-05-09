package com.example.usedAuction.service.payment;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.transactionenum.AuctionBidStateEnum;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.transactionenum.TransactionRequestStateEnum;
import com.example.usedAuction.entity.transactionenum.TransactionRequestTypeEnum;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionBidRepository;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.payment.PayInfoRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        // buyer의 잔금이 결제금액보다 많은지 확인
        if(loginUser.getMoney() < payInfoDto.getTransactionMoney()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        PayInfo savePay = new PayInfo();
        savePay.setSeller(seller);
        savePay.setBuyer(loginUser);
        savePay.setTransactionMoney(payInfoDto.getTransactionMoney());
        savePay.setTransactionRequestType(payInfoDto.getTransactionRequestType());
        savePay.setTransactionRequestState(payInfoDto.getTransactionRequestState());
        savePay.setUsedTransactionType(payInfoDto.getUsedTransactionType());
        savePay.setGeneralTransactionId(generalTransaction);
        savePay.setTransactionRequestState(TransactionRequestStateEnum.WAIT);

        try {
            // 머니 결제 요청
            payInfoRepository.save(savePay);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.GENERAL_TRANSACTION_PAYMENT_FAIL);
        }
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(savePay));

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
        
        try {
            // 거래 글 예약중으로 변경
            generalTransaction.setTransactionState(TransactionStateEnum.RESERVATION);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.PROGRESS);
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

        // 현재 로그인한 사용자가 seller buyer 둘 중 하나라도 일치하지 않으면 에러발생
        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        // 결제 정보가 있기 때문에 ID로 DB에서 조회
        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));
        // buyer의 잔금이 결제금액보다 많은지 확인
        if(loginUser.getMoney() < payInfoDto.getTransactionMoney()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }
        try {
            // 판매자 잔액 수정
            seller.setMoney(seller.getMoney() + payInfo.getTransactionMoney());
            // 구매자 잔액 수정
            loginUser.setMoney(loginUser.getMoney() - payInfo.getTransactionMoney());
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
            generalTransaction.setTransactionState(TransactionStateEnum.COMPLETE);
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

        try {
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.CANCEL);
            generalTransaction.setTransactionState(TransactionStateEnum.SALE);
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
        User seller = userRepository.findByUserIdPessimisticLock(payInfoDto.getSeller())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        User bidder = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(!(Objects.equals(bidder.getUserId(), payInfoDto.getBuyer()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        payInfoDto.setAuctionTransactionId(payInfoDto.getAuctionTransactionId());
        PayInfo savePay = new PayInfo();
        savePay.setSeller(seller);
        savePay.setBuyer(bidder);
        savePay.setTransactionMoney(payInfoDto.getTransactionMoney());
        savePay.setTransactionRequestType(payInfoDto.getTransactionRequestType());
        savePay.setTransactionRequestState(payInfoDto.getTransactionRequestState());
        savePay.setUsedTransactionType(payInfoDto.getUsedTransactionType());
        savePay.setAuctionTransactionId(auctionTransaction);
        savePay.setTransactionRequestState(TransactionRequestStateEnum.WAIT);

        // 돈 송금 기록
        PayInfo payInfo = null;
        try {
            payInfo = payInfoRepository.save(savePay);
            auctionBid.setAuctionBidState(AuctionBidStateEnum.WAIT);
            auctionTransaction.setTransactionState(TransactionStateEnum.RESERVATION);
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

        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

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

        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()) || Objects.equals(loginUser.getUserId(), buyer.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        if(seller.getMoney() < payInfoDto.getTransactionMoney()){
            throw new ApiException(ErrorEnum.INSUFFICIENT_MONEY);
        }

        // sender 잔액 수정
        try {
            seller.setMoney(seller.getMoney() + payInfo.getTransactionMoney());
            buyer.setMoney(buyer.getMoney() - payInfo.getTransactionMoney());
            auctionBid.setAuctionBidState(AuctionBidStateEnum.APPROVE);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.APPROVE);
            auctionTransaction.setTransactionState(TransactionStateEnum.COMPLETE);
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
        PayInfo payInfo = payInfoRepository.findById(payInfoDto.getPayInfoId())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_PAY_INFO));

        if(payInfo.getTransactionRequestState().equals(TransactionRequestStateEnum.APPROVE)){
            throw new ApiException(ErrorEnum.PAYMENT_COMPLETED_CANCEL_FAIL);
        }

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

        if(!(Objects.equals(loginUser.getUserId(), seller.getUserId()) || Objects.equals(loginUser.getUserId(), buyer.getUserId()))){
            throw new ApiException(ErrorEnum.FORBIDDEN_ERROR);
        }

        try {
            auctionBid.setAuctionBidState(AuctionBidStateEnum.CANCEL);
            payInfo.setTransactionRequestState(TransactionRequestStateEnum.CANCEL);
            auctionTransaction.setTransactionState(TransactionStateEnum.SALE);
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.PAYMENT_CANCEL_FAIL);
        }

        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        result.setData(DataMapper.instance.payInfoEntityToDto(payInfo));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
