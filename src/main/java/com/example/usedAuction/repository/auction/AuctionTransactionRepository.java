package com.example.usedAuction.repository.auction;

import com.example.usedAuction.entity.auction.AuctionTransaction;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AuctionTransactionRepository extends JpaRepository<AuctionTransaction,Integer> {

     Optional<AuctionTransaction> findByAuctionTransactionId(Integer auctionTransactionId);

    List<AuctionTransaction> findTop10ByTransactionStateNot(TransactionStateEnum transactionState, Sort createdAt);

    // 삭제 예정
//    List<AuctionTransaction> findAllBySellerOrderByCreatedAtDesc(User idUser);

    // 삭제 예정
//    List<AuctionTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);

//    List<AuctionTransaction> findAllByTransactionState(TransactionStateEnum transactionStateEnum, Pageable pageable);

//    List<AuctionTransaction> findAllByTransactionState(TransactionStateEnum transactionStateEnum);

    List<AuctionTransaction> findAllBySeller(User idUser, Pageable pageable);

    List<AuctionTransaction> findAllByBuyer(User idUser, Pageable pageable);

    @Query("select a from AuctionTransaction a where a.title like :keyword or a.content like :keyword order by createdAt desc")
    List<AuctionTransaction> findAllBySearch(@Param("keyword") String keyword, Pageable pageable);

    List<AuctionTransaction> findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum state,String title,String content,Pageable pageable);

    List<AuctionTransaction> findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum state,String title,String content);
}
