package com.example.usedAuction.repository.auction;


import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionBidRepository extends JpaRepository<AuctionBid, Integer> {
    AuctionBid findByAuctionTransactionIdAndBidderId(AuctionTransaction auctionTransactionId, User loginUser);
}
