package com.example.usedAuction.repository.general;


import com.example.usedAuction.entity.general.GeneralTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralTransactionRepository extends JpaRepository<GeneralTransaction,Integer> {

    GeneralTransaction findByGeneralTransactionId(Integer generalTransactionId);
}
