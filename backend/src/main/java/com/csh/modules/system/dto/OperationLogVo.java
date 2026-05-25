package com.csh.modules.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVo {
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String targetId;
    private String ip;
    private String ua;
    private LocalDateTime createdAt;
}
