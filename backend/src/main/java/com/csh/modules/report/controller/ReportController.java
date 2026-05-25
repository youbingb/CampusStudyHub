package com.csh.modules.report.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.report.dto.CreateReportReq;
import com.csh.modules.report.dto.ReportQuery;
import com.csh.modules.report.dto.ReportVo;
import com.csh.modules.report.service.ReportService;
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

@Tag(name = "举报-学生")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "提交举报")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateReportReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(reportService.create(uid, req));
    }

    @Operation(summary = "我的举报列表（分页）")
    @GetMapping("/mine")
    public R<PageResult<ReportVo>> mine(ReportQuery query) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(reportService.pageMine(uid, query));
    }

    @Operation(summary = "举报详情（仅本人可看）")
    @GetMapping("/{id}")
    public R<ReportVo> detail(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        ReportVo vo = reportService.getById(id);
        if (vo.getReporterId() == null || !vo.getReporterId().equals(uid)) {
            throw new com.csh.common.BusinessException(403, "无权查看他人举报");
        }
        return R.ok(vo);
    }

    @Operation(summary = "撤销待处理的举报")
    @DeleteMapping("/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        reportService.cancel(uid, id);
        return R.ok();
    }
}
