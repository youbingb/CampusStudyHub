package com.csh.modules.notification.dto;

import com.csh.common.constants.NotificationType;
import com.csh.modules.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private Long relatedId;
    private LocalDateTime createdAt;

    public static NotificationPayload of(Notification n) {
        return new NotificationPayload(
                n.getId(), n.getType(), n.getTitle(), n.getContent(),
                n.getRelatedId(), n.getCreatedAt());
    }
}
