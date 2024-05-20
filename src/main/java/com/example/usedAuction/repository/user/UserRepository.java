package com.example.usedAuction.repository.user;

import com.example.usedAuction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findOneWithAuthoritiesByUsername(String username);

    Optional<User> findByUsername(String username);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsernamePessimisticLock(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByUserIdPessimisticLock(@Param("userId") Integer userId);


    Optional<User> findByNickname(String nickname);

    Optional<User> findByUserId(Integer userId);
}
