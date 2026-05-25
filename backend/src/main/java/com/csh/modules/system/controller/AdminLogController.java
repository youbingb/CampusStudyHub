package com.csh.modules.system.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.system.dto.OperationLogQuery;
import com.csh.modules.system.dto.OperationLogVo;
import com.csh.modules.system.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志-管理")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "操作日志分页查询")
    @GetMapping
    public R<PageResult<OperationLogVo>> page(OperationLogQuery query) {
        return R.ok(operationLogService.page(query));
    }
}
