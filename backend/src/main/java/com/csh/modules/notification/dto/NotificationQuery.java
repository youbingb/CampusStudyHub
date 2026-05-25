package com.csh.modules.notification.dto;

import com.csh.common.constants.NotificationType;
import lombok.Data;

@Data
public class NotificationQuery {
    private Integer page = 1;
    private Integer size = 20;
    /** 0=未读 1=已读 null=全部 */
    private Integer readFlag;
    private NotificationType type;
}
