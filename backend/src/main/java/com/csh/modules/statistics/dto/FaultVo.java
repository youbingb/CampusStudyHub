package com.csh.modules.statistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ColumnWidth(20)
public class FaultVo {
    @ExcelProperty("自习室 ID")
    private Long roomId;
    @ExcelProperty("自习室名称")
    private String roomName;
    @ExcelProperty("故障总数")
    private Long totalFaults;
    @ExcelProperty("未修复")
    private Long openFaults;
    @ExcelProperty("最近故障时间")
    private LocalDateTime latestFaultAt;
}
