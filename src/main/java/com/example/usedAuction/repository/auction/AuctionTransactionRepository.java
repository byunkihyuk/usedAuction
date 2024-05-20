package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction,Integer> {

     Optional<AuctionTransaction> findByAuctionTransactionId(Integer auctionTransactionId);

    List<AuctionTransaction> findTop10ByTransactionStateNot(TransactionStateEnum transactionState, Sort createdAt);
    // 삭제 예정
    List<AuctionTransaction> findAllBySellerOrderByCreatedAtDesc(User idUser);
    // 삭제 예정
    List<AuctionTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);

    List<AuctionTransaction> findAllByTransactionState(TransactionStateEnum transactionStateEnum, Pageable pageable);

    List<AuctionTransaction> findAllByTransactionState(TransactionStateEnum transactionStateEnum);

    List<AuctionTransaction> findAllBySeller(User idUser, Pageable pageable);

    List<AuctionTransaction> findAllByBuyer(User idUser, Pageable pageable);
}
