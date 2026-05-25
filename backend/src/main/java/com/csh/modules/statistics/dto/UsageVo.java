package com.csh.modules.statistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(18)
public class UsageVo {
    @ExcelProperty("用户 ID")
    private Long userId;
    @ExcelProperty("用户名")
    private String username;
    @ExcelProperty("姓名")
    private String realName;
    @ExcelProperty("学号")
    private String studentNo;
    @ExcelProperty("预约总数")
    private Long reservationCount;
    @ExcelProperty("完成数")
    private Long completedCount;
    @ExcelProperty("违约数")
    private Long noShowCount;
    @ExcelProperty("使用小时")
    private Double totalHours;
}
