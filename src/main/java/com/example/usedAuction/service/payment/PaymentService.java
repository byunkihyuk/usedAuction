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

}
