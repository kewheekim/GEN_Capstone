package com.gen.rally.repository;

import com.gen.rally.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
}

