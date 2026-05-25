package com.csh.modules.room.dto;

import lombok.Data;

@Data
public class RoomVo {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String openTime;
    private String closeTime;
    private Integer status;
    private String description;
    private Integer totalSeats;
    private Integer availableSeats;
}
