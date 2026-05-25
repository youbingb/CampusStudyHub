package com.csh.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csh.common.constants.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    @TableField("read_flag")
    private Integer readFlag;
    private Long relatedId;
    private LocalDateTime createdAt;
}
