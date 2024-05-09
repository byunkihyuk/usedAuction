package com.example.usedAuction.repository.auction;


import com.example.usedAuction.entity.auction.AuctionBid;
import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuctionBidRepository extends JpaRepository<AuctionBid, Integer> {
    Optional<AuctionBid> findByAuctionTransactionIdAndBidder(AuctionTransaction auctionTransactionId, User loginUser);

    List<AuctionBid> findByAuctionTransactionId(AuctionTransaction auctionTransaction);

    List<AuctionBid> findByBidder(User loginUser);
}
