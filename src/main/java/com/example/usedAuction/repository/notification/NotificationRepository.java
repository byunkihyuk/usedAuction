package com.example.usedAuction.repository.notification;

import com.example.usedAuction.entity.notification.Notification;
import com.example.usedAuction.entity.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(User loginUser, Pageable pageable);

    Long countByUserIdAndReadornot(User loginUser, boolean b);

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.readornot = true where n.userId= :user")
    int bulkReadAllNotification(@Param("user") User loginUser);
}
