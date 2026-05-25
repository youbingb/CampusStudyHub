package com.csh.modules.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVo {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    /** 1=已发布 0=草稿 */
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
