package com.csh.modules.room.dto;

import lombok.Data;

@Data
public class UpdateRoomReq {
    private String name;
    private String location;
    private Integer capacity;
    private String openTime;
    private String closeTime;
    private Integer status;
    private String description;
}
