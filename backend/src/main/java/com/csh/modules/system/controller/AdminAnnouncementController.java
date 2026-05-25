package com.csh.modules.system.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.system.dto.AnnouncementQuery;
import com.csh.modules.system.dto.AnnouncementVo;
import com.csh.modules.system.dto.CreateAnnouncementReq;
import com.csh.modules.system.dto.UpdateAnnouncementReq;
import com.csh.modules.system.service.AnnouncementService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公告-管理")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "管理端：公告列表（含草稿）")
    @GetMapping
    public R<PageResult<AnnouncementVo>> page(AnnouncementQuery query) {
        return R.ok(announcementService.pageAdmin(query));
    }

    @Operation(summary = "管理端：公告详情")
    @GetMapping("/{id}")
    public R<AnnouncementVo> detail(@PathVariable Long id) {
        return R.ok(announcementService.getById(id, false));
    }

    @Operation(summary = "新增公告（publishNow=true 立即发布，false 存为草稿）")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateAnnouncementReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(announcementService.create(uid, req));
    }

    @Operation(summary = "更新公告标题/内容")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid UpdateAnnouncementReq req) {
        announcementService.update(id, req);
        return R.ok();
    }

    @Operation(summary = "发布草稿")
    @PostMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return R.ok();
    }

    @Operation(summary = "下架公告")
    @PostMapping("/{id}/unpublish")
    public R<Void> unpublish(@PathVariable Long id) {
        announcementService.unpublish(id);
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.ok();
    }
}
