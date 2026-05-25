package com.csh.modules.system.dto;

import lombok.Data;

@Data
public class AnnouncementQuery {
    private String keyword;
    /** 1=已发布 0=草稿，null=不限 */
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
