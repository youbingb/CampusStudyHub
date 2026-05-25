package com.csh.modules.statistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(14)
public class PopularHourVo {
    @ExcelProperty("小时（0-23）")
    private Integer hour;
    @ExcelProperty("预约数")
    private Long reservationCount;
}
