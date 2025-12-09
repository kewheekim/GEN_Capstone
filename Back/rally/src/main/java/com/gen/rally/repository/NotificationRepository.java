package com.gen.rally.repository;

import com.gen.rally.entity.Notification;
import com.gen.rally.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    int countByUserUserIdAndTypeAndIsReadFalse(String userId, State type);

    @Modifying
    @Query("update Notification n " + "set n.isRead = true " +
            "where n.user.userId = :userId " + "and n.type = :type " + "and n.isRead = false")
    int markAsRead(String userId, State type);
}

