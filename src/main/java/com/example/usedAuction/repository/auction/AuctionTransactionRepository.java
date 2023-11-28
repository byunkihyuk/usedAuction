package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction,Integer> {

     Optional<AuctionTransaction> findByAuctionTransactionId(Integer auctionTransactionId);

    List<AuctionTransaction> findTop10ByTransactionStateNot(String transactionState, Sort createdAt);

    List<AuctionTransaction> findAllBySellerOrderByCreatedAtDesc(User idUser);
  
    List<AuctionTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);
}
