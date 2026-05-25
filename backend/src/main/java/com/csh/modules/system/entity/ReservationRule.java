package com.csh.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reservation_rule")
public class ReservationRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer maxDaily;
    private Integer maxAdvanceDays;
    private Integer minCredit;
    private Integer checkInGraceMin;
    private Integer maxDurationHours;
    private Integer noShowCreditPenalty;
    private LocalDateTime updatedAt;
}
