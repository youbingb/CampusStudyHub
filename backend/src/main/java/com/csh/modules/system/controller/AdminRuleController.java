package com.csh.modules.system.controller;

import com.csh.common.R;
import com.csh.modules.system.dto.RuleVo;
import com.csh.modules.system.dto.UpdateRuleReq;
import com.csh.modules.system.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约规则-管理")
@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class AdminRuleController {

    private final RuleService ruleService;

    @Operation(summary = "管理端读取当前预约规则")
    @GetMapping
    public R<RuleVo> get() {
        return R.ok(ruleService.current());
    }

    @Operation(summary = "更新预约规则（字段可选，仅传入项被修改）")
    @PutMapping
    public R<RuleVo> update(@RequestBody @Valid UpdateRuleReq req) {
        return R.ok(ruleService.update(req));
    }
}
