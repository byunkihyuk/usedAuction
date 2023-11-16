package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.TransactionImage;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.auction.AuctionTransactionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionTransactionImageRepository extends JpaRepository<AuctionTransactionImage,Integer> {

    List<AuctionTransactionImage> findAllByAuctionTransactionIdOrderByImageSeq(AuctionTransaction auctionTransaction);

    List<AuctionTransactionImage> findAllByAuctionTransactionId(AuctionTransaction auctionTransaction);
}
