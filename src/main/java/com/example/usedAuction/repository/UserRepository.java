package com.example.usedAuction.repository;

import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findOneWithAuthoritiesByUsername(String username);

    User findByUsername(String username);

    User findByNickname(String nickname);
}
