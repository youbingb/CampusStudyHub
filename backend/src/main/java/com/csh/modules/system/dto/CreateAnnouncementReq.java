package com.csh.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAnnouncementReq {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128)
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** true 直接发布；false 先存为草稿。默认 false。 */
    private Boolean publishNow;
}
