package com.csh.modules.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoomReq {
    @NotBlank(message = "房间名称不能为空")
    private String name;

    private String location;

    @Min(value = 0, message = "容量不能为负")
    private Integer capacity;

    private String openTime;
    private String closeTime;
    private String description;
}
