package com.example.usedAuction.repository.payment;

import com.example.usedAuction.dto.payment.PayInfoDto;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PayInfoRepository extends JpaRepository<PayInfo,Integer> {


    List<PayInfo> findAllBySellerOrBuyerOrderByTransactionUpdateTime(User seller,User buyer);

    List<PayInfo> findAllByGeneralTransactionId(GeneralTransaction generalTransactionId);

    Optional<PayInfo> findByGeneralTransactionIdAndBuyer(GeneralTransaction generalTransaction, User loginUser);

    Optional<PayInfo> findByAuctionTransactionIdAndBuyer(AuctionTransaction auctionTransaction, User buyer);
}
