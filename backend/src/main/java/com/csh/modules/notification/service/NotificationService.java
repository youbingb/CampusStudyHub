package com.csh.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.PageResult;
import com.csh.common.constants.NotificationType;
import com.csh.modules.notification.dto.NotificationPayload;
import com.csh.modules.notification.dto.NotificationQuery;
import com.csh.modules.notification.dto.NotificationVo;
import com.csh.modules.notification.entity.Notification;
import com.csh.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 跨模块站内通知服务（Agent A owner）。
 * 契约见 docs/AGENTS.md §3.1，send 方法签名不允许更改。
 * 写 notification 表 + 调 WsPushService 推 /user/queue/notifications。
 *
 * <p>同时承担 NotificationController 的业务实现：list/unreadCount/markRead/markAllRead。
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

    public PageResult<NotificationVo> list(Long userId, NotificationQuery q) {
        Page<Notification> p = new Page<>(q.getPage() == null ? 1 : q.getPage(),
                q.getSize() == null ? 20 : q.getSize());
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(q.getReadFlag() != null, Notification::getReadFlag, q.getReadFlag())
                .eq(q.getType() != null, Notification::getType, q.getType())
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> res = notificationMapper.selectPage(p, w);
        PageResult<NotificationVo> r = new PageResult<>();
        r.setTotal(res.getTotal());
        r.setPages(res.getPages());
        r.setCurrent(res.getCurrent());
        r.setSize(res.getSize());
        r.setRecords(res.getRecords().stream().map(NotificationVo::of).toList());
        return r;
    }

    public long unreadCount(Long userId) {
        Long c = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadFlag, 0));
        return c == null ? 0 : c;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long id) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .set(Notification::getReadFlag, 1)
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadFlag, 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public int markAllRead(Long userId) {
        return notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .set(Notification::getReadFlag, 1)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadFlag, 0));
    }
}
