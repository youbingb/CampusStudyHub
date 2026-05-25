package com.csh.modules.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessReportReq {

    /** APPROVE = 核实通过；REJECT = 驳回 */
    @NotNull(message = "处理动作不能为空")
    private Action action;

    @Size(max = 500, message = "处理结果说明不超过 500 字")
    private String result;

    /** 信誉分变化量；正数加分（举报人奖励），负数扣分（被举报人惩罚），0 表示不动信誉 */
    @Min(value = -50)
    @Max(value = 50)
    private Integer creditDelta;

    @Size(max = 255)
    private String creditReason;

    public enum Action { APPROVE, REJECT }
}
