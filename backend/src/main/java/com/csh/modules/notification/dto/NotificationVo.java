package com.csh.modules.notification.dto;

import com.csh.common.constants.NotificationType;
import com.csh.modules.notification.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVo {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private Integer readFlag;
    private Long relatedId;
    private LocalDateTime createdAt;

    public static NotificationVo of(Notification n) {
        NotificationVo vo = new NotificationVo();
        vo.id = n.getId();
        vo.type = n.getType();
        vo.title = n.getTitle();
        vo.content = n.getContent();
        vo.readFlag = n.getReadFlag();
        vo.relatedId = n.getRelatedId();
        vo.createdAt = n.getCreatedAt();
        return vo;
    }
}
