package com.csh.modules.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateRuleReq {

    @Min(value = 1, message = "每日预约数至少 1")
    @Max(value = 20, message = "每日预约数不超过 20")
    private Integer maxDaily;

    @Min(value = 0, message = "可提前天数至少 0")
    @Max(value = 30, message = "可提前天数不超过 30")
    private Integer maxAdvanceDays;

    @Min(value = 0, message = "信誉门槛最低 0")
    @Max(value = 100, message = "信誉门槛最高 100")
    private Integer minCredit;

    @Min(value = 0, message = "迟到宽限分钟最低 0")
    @Max(value = 120, message = "迟到宽限分钟最高 120")
    private Integer checkInGraceMin;

    @Min(value = 1, message = "单次最长小时至少 1")
    @Max(value = 12, message = "单次最长小时不超过 12")
    private Integer maxDurationHours;

    @Min(value = 0, message = "违约扣分至少 0")
    @Max(value = 50, message = "违约扣分不超过 50")
    private Integer noShowCreditPenalty;
}
