package com.csh.modules.statistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(18)
public class OccupancyVo {
    @ExcelProperty("自习室 ID")
    private Long roomId;
    @ExcelProperty("自习室名称")
    private String roomName;
    @ExcelProperty("座位数")
    private Integer capacity;
    @ExcelProperty("预约总数")
    private Long totalReservations;
    @ExcelProperty("已签到/完成")
    private Long completedReservations;
    @ExcelProperty("使用小时合计")
    private Double totalSeatHours;
}
