package com.csh.modules.report.dto;

import com.csh.common.constants.ReportStatus;
import lombok.Data;

@Data
public class ReportQuery {
    private ReportStatus status;
    private String type;
    private String keyword;
    private Long reporterId;
    private Long targetUserId;
    private Integer page = 1;
    private Integer size = 10;
}
