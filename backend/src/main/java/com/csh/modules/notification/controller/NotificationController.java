package com.csh.modules.notification.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.notification.dto.NotificationQuery;
import com.csh.modules.notification.dto.NotificationVo;
import com.csh.modules.notification.service.NotificationService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "站内通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "分页查询当前用户通知")
    @GetMapping
    public R<PageResult<NotificationVo>> list(@ModelAttribute NotificationQuery q) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(notificationService.list(uid, q));
    }

    @Operation(summary = "当前用户未读通知数")
    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(notificationService.unreadCount(uid));
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        notificationService.markRead(uid, id);
        return R.ok();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public R<Integer> markAllRead() {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(notificationService.markAllRead(uid));
    }
}
