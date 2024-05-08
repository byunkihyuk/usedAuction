package com.example.usedAuction.repository.payment;

import com.example.usedAuction.entity.payment.PayInfo;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PayInfoRepository extends JpaRepository<PayInfo,Integer> {


    List<PayInfo> findAllBySellerOrBuyerOrderByTransactionUpdateTime(User seller,User buyer);
}
