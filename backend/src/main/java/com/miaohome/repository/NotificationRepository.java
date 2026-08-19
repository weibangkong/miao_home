package com.miaohome.repository;

import com.miaohome.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Notification> findByAdopterIdOrderByCreatedAtDesc(Long adopterId);
    List<Notification> findByAdopterIdAndIsReadOrderByCreatedAtDesc(Long adopterId, Boolean isRead);
    long countByAdopterIdAndIsRead(Long adopterId, Boolean isRead);
}
