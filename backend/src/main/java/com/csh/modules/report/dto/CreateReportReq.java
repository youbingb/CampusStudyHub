package com.csh.modules.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportReq {

    @NotBlank(message = "举报类型不能为空")
    @Size(max = 32)
    private String type;

    @NotBlank(message = "举报描述不能为空")
    @Size(max = 500)
    private String description;

    private Long targetUserId;

    private Long reservationId;

    private Long seatId;

    @Size(max = 500)
    private String evidenceUrl;
}
