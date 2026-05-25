package com.csh.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkFaultReq {
    @NotBlank(message = "故障原因不能为空")
    private String reason;
}
