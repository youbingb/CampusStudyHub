package com.csh.modules.inspection.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.inspection.dto.CreateInspectionReq;
import com.csh.modules.inspection.dto.InspectionQuery;
import com.csh.modules.inspection.dto.InspectionVo;
import com.csh.modules.inspection.service.InspectionService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "巡检-管理")
@RestController
@RequestMapping("/api/admin/inspections")
@RequiredArgsConstructor
public class AdminInspectionController {

    private final InspectionService inspectionService;

    @Operation(summary = "巡检记录分页查询")
    @GetMapping
    public R<PageResult<InspectionVo>> page(InspectionQuery query) {
        return R.ok(inspectionService.page(query));
    }

    @Operation(summary = "巡检记录详情")
    @GetMapping("/{id}")
    public R<InspectionVo> detail(@PathVariable Long id) {
        return R.ok(inspectionService.getById(id));
    }

    @Operation(summary = "新增巡检（issues 中的座位会被标记为 FAULT 并广播）")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateInspectionReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(inspectionService.create(uid, req));
    }

    @Operation(summary = "删除巡检记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        inspectionService.delete(id);
        return R.ok();
    }
}
