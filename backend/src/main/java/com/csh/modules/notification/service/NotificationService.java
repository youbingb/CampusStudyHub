package com.csh.modules.notification.service;

import com.csh.common.constants.NotificationType;
import com.csh.modules.notification.dto.NotificationPayload;
import com.csh.modules.notification.entity.Notification;
import com.csh.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 跨模块站内通知服务（Agent A owner）。
 * 契约见 docs/AGENTS.md §3.1，签名不允许更改。
 * 写 notification 表 + 调 WsPushService 推 /user/queue/notifications。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final WsPushService wsPushService;

    public void send(Long userId, NotificationType type, String title, String content) {
        send(userId, type, title, content, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void send(Long userId, NotificationType type, String title, String content, Long relatedId) {
        if (userId == null || type == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setReadFlag(0);
        n.setRelatedId(relatedId);
        n.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(n);
        wsPushService.publishToUser(userId, NotificationPayload.of(n));
    }
}
