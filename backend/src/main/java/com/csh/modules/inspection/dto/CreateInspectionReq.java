package com.csh.modules.inspection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateInspectionReq {

    @NotNull(message = "自习室不能为空")
    private Long roomId;

    @Size(max = 500)
    private String content;

    /** 巡检中发现故障的座位 id 列表（可空）。每个都会被标记为 FAULT 并广播。 */
    private List<Long> issues;
}
