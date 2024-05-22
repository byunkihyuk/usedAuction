package com.example.usedAuction.repository.general;


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

public interface GeneralTransactionRepository extends JpaRepository<GeneralTransaction,Integer> {

    GeneralTransaction findByGeneralTransactionId(Integer generalTransactionId);

    List<GeneralTransaction> findAllByTransactionState(TransactionStateEnum state,Pageable pageable);

    List<GeneralTransaction> findAllByTransactionState(TransactionStateEnum state);

    List<GeneralTransaction> findTop10ByTransactionStateNot(TransactionStateEnum transactionState, Sort viewCount);

    // 삭제 예정
    List<GeneralTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);
    // 삭제 예정
    List<GeneralTransaction>  findAllBySellerOrderByCreatedAtDesc(User idUser);

    List<GeneralTransaction> findAllByBuyer(User buyer, Pageable pageable);

    List<GeneralTransaction> findAllBySeller(User seller,Pageable pageable);

    @Query("select g from GeneralTransaction g where g.title like :keyword or g.content like :keyword  order by createdAt desc")
    List<GeneralTransaction> findAllBySearch(@Param("keyword") String keyword, Pageable pageable);

    List<GeneralTransaction> findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum state,String title,String content,Pageable pageable);

    List<GeneralTransaction> findAllByTransactionStateAndTitleContainingOrContentContaining(TransactionStateEnum state,String title,String content);
}
