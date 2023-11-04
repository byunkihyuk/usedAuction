package com.example.usedAuction.repository;


import com.example.usedAuction.entity.GeneralTransaction;
import com.example.usedAuction.entity.GeneralTransactionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneralTransactionRepository extends JpaRepository<GeneralTransaction,Integer> {

    GeneralTransaction findByGeneralTransactionId(Integer generalTransactionId);
}
