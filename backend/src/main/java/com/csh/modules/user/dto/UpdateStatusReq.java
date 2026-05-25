package com.csh.modules.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusReq {

    /** 1 启用 / 0 禁用 */
    @NotNull(message = "status 不能为空")
    private Integer status;
}
