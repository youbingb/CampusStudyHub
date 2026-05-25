package com.csh.modules.statistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(18)
public class ViolationVo {
    @ExcelProperty("用户 ID")
    private Long userId;
    @ExcelProperty("用户名")
    private String username;
    @ExcelProperty("姓名")
    private String realName;
    @ExcelProperty("学号")
    private String studentNo;
    @ExcelProperty("当前信誉")
    private Integer creditScore;
    @ExcelProperty("违规扣分次数")
    private Long violationCount;
    @ExcelProperty("扣分合计")
    private Long totalDeduction;
}
