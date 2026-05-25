package com.csh.modules.system.controller;

import com.csh.common.R;
import com.csh.modules.system.dto.RuleVo;
import com.csh.modules.system.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约规则-学生")
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "学生端读取当前生效的预约规则（用于提交前的客户端校验提示）")
    @GetMapping("/current")
    public R<RuleVo> current() {
        return R.ok(ruleService.current());
    }
}
