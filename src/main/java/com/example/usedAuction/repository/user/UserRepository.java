package com.example.usedAuction.repository.user;

import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findOneWithAuthoritiesByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByNickname(String nickname);
}
