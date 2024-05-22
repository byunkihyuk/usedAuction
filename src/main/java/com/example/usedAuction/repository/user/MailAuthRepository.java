package com.example.usedAuction.repository.user;

import com.example.usedAuction.entity.user.MailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface MailAuthRepository extends JpaRepository< MailAuth,Long> {

}
