package com.csh.modules.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleVo {
    private Long id;
    private Integer maxDaily;
    private Integer maxAdvanceDays;
    private Integer minCredit;
    private Integer checkInGraceMin;
    private Integer maxDurationHours;
    private Integer noShowCreditPenalty;
    private LocalDateTime updatedAt;
}
