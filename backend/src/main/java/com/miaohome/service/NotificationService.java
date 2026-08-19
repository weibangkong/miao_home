package com.miaohome.service;

import com.miaohome.entity.Adopter;
import com.miaohome.entity.Notification;
import com.miaohome.exception.BusinessException;
import com.miaohome.exception.ErrorCode;
import com.miaohome.repository.AdopterRepository;
import com.miaohome.repository.CatRepository;
import com.miaohome.repository.NotificationRepository;
import com.miaohome.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知业务逻辑 + SSE 实时推送
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdopterRepository adopterRepository;
    private final CatRepository catRepository;

    /** SSE 连接池，key = tenantId:adopterId */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository notificationRepository,
                               AdopterRepository adopterRepository,
                               CatRepository catRepository) {
        this.notificationRepository = notificationRepository;
        this.adopterRepository = adopterRepository;
        this.catRepository = catRepository;
    }

    /** 获取当前租户下所有通知，按创建时间倒序；无租户时返回全部 */
    public List<Notification> getNotifications() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        }
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 获取指定认养人的所有通知 */
    public List<Notification> getNotificationsByAdopter(Long adopterId) {
        return notificationRepository.findByAdopterIdOrderByCreatedAtDesc(adopterId);
    }

    /** 获取指定认养人的未读通知数量 */
    public long getUnreadCount(Long adopterId) {
        return notificationRepository.countByAdopterIdAndIsRead(adopterId, false);
    }

    /** 将单条通知标记为已读 */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND, "通知不存在"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /** 将指定认养人的全部通知标记为已读 */
    public void markAllAsRead(Long adopterId) {
        List<Notification> unreadList = notificationRepository
                .findByAdopterIdAndIsReadOrderByCreatedAtDesc(adopterId, false);
        for (Notification n : unreadList) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }

    /**
     * 向指定认养人发送单条通知
     * <ul>
     *   <li>保存通知到数据库</li>
     *   <li>通过 SSE 实时推送给已连接的客户端</li>
     * </ul>
     */
    public Notification sendNotification(Long adopterId, Long catId, String title, String content) {
        Adopter adopter = adopterRepository.findById(adopterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADOPTER_FOR_NOTIFICATION_NOT_FOUND, "认养人不存在"));

        Notification notification = new Notification();
        notification.setTenantId(adopter.getTenantId());
        notification.setAdopterId(adopterId);
        notification.setCatId(catId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        // 通过 SSE 推送
        String emitterKey = adopter.getTenantId() + ":" + adopterId;
        SseEmitter emitter = emitters.get(emitterKey);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                // 推送失败说明连接已断开，移除 emitter
                emitters.remove(emitterKey);
            }
        }

        return notification;
    }

    /** 向认养了指定猫咪的所有有效认养者发送通知 */
    public Notification sendNotificationToCatAdopters(Long catId, String title, String content) {
        List<Adopter> adopters = adopterRepository.findByCatId(catId);
        for (Adopter adopter : adopters) {
            if (Boolean.TRUE.equals(adopter.getIsActive())) {
                sendNotification(adopter.getId(), catId, title, content);
            }
        }
        // 返回最后一条通知（仅用于接口响应）
        Long tenantId = TenantContext.getTenantId();
        List<Notification> notifications = (tenantId != null)
                ? notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                : notificationRepository.findAllByOrderByCreatedAtDesc();
        return notifications.stream().findFirst().orElse(null);
    }

    /**
     * SSE 订阅接口
     * <p>客户端建立长连接后，后端通过该连接实时推送通知。</p>
     * @param adopterId 认养人 ID
     * @return SseEmitter（永不超时）
     */
    public SseEmitter subscribe(Long adopterId) {
        Long tenantId = TenantContext.getTenantId();
        SseEmitter emitter = new SseEmitter(0L); // 0 = 永不超时
        String key = tenantId + ":" + adopterId;
        emitters.put(key, emitter);

        // 连接关闭/超时/出错时自动清理
        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> emitters.remove(key));
        emitter.onError(e -> emitters.remove(key));

        try {
            emitter.send(SseEmitter.event().name("connected").data("连接成功"));
        } catch (IOException e) {
            emitters.remove(key);
        }

        return emitter;
    }
}
