package com.csh.modules.report.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.report.dto.ProcessReportReq;
import com.csh.modules.report.dto.ReportQuery;
import com.csh.modules.report.dto.ReportVo;
import com.csh.modules.report.service.ReportService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "举报-管理")
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "举报列表（分页 + 筛选）")
    @GetMapping
    public R<PageResult<ReportVo>> page(ReportQuery query) {
        return R.ok(reportService.pageAdmin(query));
    }

    @Operation(summary = "举报详情")
    @GetMapping("/{id}")
    public R<ReportVo> detail(@PathVariable Long id) {
        return R.ok(reportService.getById(id));
    }

    @Operation(summary = "处理举报：核实通过或驳回")
    @PostMapping("/{id}/process")
    public R<Void> process(@PathVariable Long id, @RequestBody @Valid ProcessReportReq req) {
        Long handlerId = LoginUserHolder.requireCurrent().getId();
        reportService.process(handlerId, id, req);
        return R.ok();
    }
}
