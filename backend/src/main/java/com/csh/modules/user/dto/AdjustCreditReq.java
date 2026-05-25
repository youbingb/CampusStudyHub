package com.csh.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustCreditReq {

    @NotNull(message = "delta 不能为空")
    private Integer delta;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
