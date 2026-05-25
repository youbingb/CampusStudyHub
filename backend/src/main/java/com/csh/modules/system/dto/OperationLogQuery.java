package com.csh.modules.system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OperationLogQuery {
    private String module;
    private String action;
    private String username;
    private Long userId;
    private LocalDate from;
    private LocalDate to;
    private Integer page = 1;
    private Integer size = 20;
}
