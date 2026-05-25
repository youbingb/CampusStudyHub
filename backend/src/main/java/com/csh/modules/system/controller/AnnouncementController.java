package com.csh.modules.system.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.system.dto.AnnouncementQuery;
import com.csh.modules.system.dto.AnnouncementVo;
import com.csh.modules.system.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公告-学生")
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "学生端：已发布公告分页")
    @GetMapping
    public R<PageResult<AnnouncementVo>> page(AnnouncementQuery query) {
        return R.ok(announcementService.pageStudent(query));
    }

    @Operation(summary = "学生端：公告详情（仅已发布）")
    @GetMapping("/{id}")
    public R<AnnouncementVo> detail(@PathVariable Long id) {
        return R.ok(announcementService.getById(id, true));
    }

    @Operation(summary = "学生端：当前生效公告 top N，用于首页/Layout 轮播")
    @GetMapping("/active")
    public R<List<AnnouncementVo>> active(
            @Parameter(description = "条数，默认 5，最大 20") @RequestParam(defaultValue = "5") Integer limit) {
        return R.ok(announcementService.listActive(limit));
    }
}
