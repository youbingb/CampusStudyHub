package com.csh.modules.user.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.user.dto.AdjustCreditReq;
import com.csh.modules.user.dto.UpdateStatusReq;
import com.csh.modules.user.dto.UserQuery;
import com.csh.modules.user.dto.UserVo;
import com.csh.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理-用户")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "用户分页列表（keyword 模糊匹配用户名/姓名/学号/手机）")
    @GetMapping
    public R<PageResult<UserVo>> list(@ModelAttribute UserQuery q) {
        return R.ok(userService.adminList(q));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusReq req) {
        userService.updateStatus(id, req.getStatus());
        return R.ok();
    }

    @Operation(summary = "管理员手动调整信誉分（委托 CreditService，会写 credit_log + 站内通知）")
    @PostMapping("/{id}/credit")
    public R<Integer> adjustCredit(@PathVariable Long id, @RequestBody @Valid AdjustCreditReq req) {
        return R.ok(userService.adjustCredit(id, req.getDelta(), req.getReason()));
    }
}
