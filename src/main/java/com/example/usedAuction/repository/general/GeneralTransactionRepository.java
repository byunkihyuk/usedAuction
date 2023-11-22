package com.example.usedAuction.repository.general;


import com.example.usedAuction.dto.General.GeneralTransactionDto;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneralTransactionRepository extends JpaRepository<GeneralTransaction,Integer> {

    GeneralTransaction findByGeneralTransactionId(Integer generalTransactionId);

    List<GeneralTransaction> findTop10ByTransactionStateNot(String transactionState, Sort viewCount);

    List<GeneralTransaction> findAllByBuyerOrderByCreatedAtDesc(User idUser);
}
