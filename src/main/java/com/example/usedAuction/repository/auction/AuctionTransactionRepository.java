package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction,Integer> {

}
