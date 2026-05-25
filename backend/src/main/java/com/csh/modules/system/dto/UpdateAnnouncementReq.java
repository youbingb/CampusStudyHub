package com.csh.modules.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAnnouncementReq {

    @Size(max = 128)
    private String title;

    private String content;
}
