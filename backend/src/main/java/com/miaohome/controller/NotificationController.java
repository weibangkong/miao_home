package com.miaohome.controller;

import com.miaohome.dto.ApiResult;
import com.miaohome.dto.NotificationResponse;
import com.miaohome.entity.Notification;
import com.miaohome.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知相关接口 + SSE 实时推送订阅
 */
@Tag(name = "通知管理", description = "通知查询、已读标记、发送与 SSE 实时订阅")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** 获取所有通知（当前租户下） */
    @Operation(summary = "获取所有通知")
    @GetMapping("/list")
    public ApiResult<List<NotificationResponse>> list() {
        List<Notification> notifications = notificationService.getNotifications();
        List<NotificationResponse> result = notifications.stream()
                .map(this::toNotificationResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /** 获取指定认养人的通知列表 */
    @Operation(summary = "获取指定认养人的通知列表")
    @GetMapping("/adopter/{adopterId}/list")
    public ApiResult<List<NotificationResponse>> getByAdopter(
            @Parameter(description = "认养人 ID") @PathVariable Long adopterId) {
        List<Notification> notifications = notificationService.getNotificationsByAdopter(adopterId);
        List<NotificationResponse> result = notifications.stream()
                .map(this::toNotificationResponse).collect(Collectors.toList());
        return ApiResult.success(result);
    }

    /** 获取指定认养人的未读通知数 */
    @Operation(summary = "获取指定认养人的未读通知数")
    @GetMapping("/adopter/{adopterId}/unread/count")
    public ApiResult<Long> getUnreadCount(
            @Parameter(description = "认养人 ID") @PathVariable Long adopterId) {
        long count = notificationService.getUnreadCount(adopterId);
        return ApiResult.success(count);
    }

    /** 标记单条通知为已读 */
    @Operation(summary = "标记单条通知为已读")
    @PutMapping(value = "/{id}/read", consumes = "application/json")
    public ApiResult<Void> markAsRead(@Parameter(description = "通知 ID") @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResult.success(null);
    }

    /** 将指定认养人的所有通知标记为已读 */
    @Operation(summary = "将指定认养人的所有通知标记为已读")
    @PutMapping(value = "/adopter/{adopterId}/read/all", consumes = "application/json")
    public ApiResult<Void> markAllAsRead(
            @Parameter(description = "认养人 ID") @PathVariable Long adopterId) {
        notificationService.markAllAsRead(adopterId);
        return ApiResult.success(null);
    }

    /** 向指定认养人发送通知（同时会通过 SSE 实时推送） */
    @Operation(summary = "向指定认养人发送通知")
    @PostMapping(value = "/send", consumes = "application/json")
    public ApiResult<NotificationResponse> send(
            @Parameter(description = "通知内容") @RequestBody Notification notification) {
        Notification saved = notificationService.sendNotification(
                notification.getAdopterId(),
                notification.getCatId(),
                notification.getTitle(),
                notification.getContent());
        return ApiResult.success(toNotificationResponse(saved));
    }

    /** 向认养了某只猫咪的所有认养者发送通知 */
    @Operation(summary = "向认养了某只猫咪的所有认养者发送通知")
    @PostMapping(value = "/cat/{catId}/adopters/send", consumes = "application/json")
    public ApiResult<Void> sendToCatAdopters(
            @Parameter(description = "猫咪 ID") @PathVariable Long catId,
            @Parameter(description = "通知内容") @RequestBody Notification notification) {
        notificationService.sendNotificationToCatAdopters(catId, notification.getTitle(), notification.getContent());
        return ApiResult.success(null);
    }

    /**
     * SSE 实时推送订阅
     * <p>客户端通过 EventSource 连接此接口，后端有新通知时会自动推送。</p>
     */
    @Operation(summary = "订阅 SSE 实时推送")
    @GetMapping(value = "/subscribe/{adopterId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Parameter(description = "认养人 ID") @PathVariable Long adopterId) {
        return notificationService.subscribe(adopterId);
    }

    // ---- 内部转换方法 ----

    /** Notification 实体 → NotificationResponse */
    private NotificationResponse toNotificationResponse(Notification n) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(n.getId());
        resp.setTenantId(n.getTenantId());
        resp.setAdopterId(n.getAdopterId());
        resp.setCatId(n.getCatId());
        resp.setTitle(n.getTitle());
        resp.setContent(n.getContent());
        resp.setIsRead(n.getIsRead());
        resp.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        return resp;
    }
}
