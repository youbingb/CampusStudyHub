package com.csh.modules.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchCreateSeatReq {
    @NotNull(message = "房间 ID 不能为空")
    private Long roomId;

    @NotNull
    @Min(value = 1, message = "行数至少 1")
    private Integer rows;

    @NotNull
    @Min(value = 1, message = "列数至少 1")
    private Integer cols;

    /** 座位编号前缀，默认 A，将拼成 A01/A02... */
    private String prefix;

    private String feature;
}
