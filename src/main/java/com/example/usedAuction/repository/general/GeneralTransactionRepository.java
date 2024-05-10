package com.example.usedAuction.repository.general;


import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.transactionenum.TransactionStateEnum;
import com.example.usedAuction.entity.user.User;
import org.hibernate.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface GeneralTransactionRepository extends JpaRepository<GeneralTransaction,Integer> {

    GeneralTransaction findByGeneralTransactionId(Integer generalTransactionId);

    List<GeneralTransaction> findAllByTransactionState(TransactionStateEnum state,Pageable pageable);

    List<GeneralTransaction> findTop10ByTransactionStateNot(TransactionStateEnum transactionState, Sort viewCount);

    List<GeneralTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);

    List<GeneralTransaction>  findAllBySellerOrderByCreatedAtDesc(User idUser);
}
