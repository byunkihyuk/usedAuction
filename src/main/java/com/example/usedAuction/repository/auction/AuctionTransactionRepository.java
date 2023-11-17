package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction,Integer> {

     Optional<AuctionTransaction> findByAuctionTransactionId(Integer auctionTransactionId);

    List<AuctionTransaction> findTop10ByTransactionStateNot(String transactionState, Sort createdAt);
}
