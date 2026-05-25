package com.csh.modules.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeatReq {
    @NotNull(message = "房间 ID 不能为空")
    private Long roomId;

    @NotBlank(message = "座位编号不能为空")
    private String seatNo;

    @NotNull(message = "行号不能为空")
    private Integer rowNo;

    @NotNull(message = "列号不能为空")
    private Integer colNo;

    private String feature;
}
