package com.example.usedAuction.repository.general;

import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.general.GeneralTransactionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneralTransactionImageRepository extends JpaRepository<GeneralTransactionImage, Integer> {

  List<GeneralTransactionImage> findAllByGeneralTransactionId(GeneralTransaction generalTransactionId);

  List<GeneralTransactionImage> findAllByGeneralTransactionIdOrderByImageSeq(GeneralTransaction generalTransaction);
}
