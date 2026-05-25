package com.csh.modules.user.controller;

import com.csh.common.R;
import com.csh.modules.user.dto.LoginReq;
import com.csh.modules.user.dto.LoginResp;
import com.csh.modules.user.dto.RegisterReq;
import com.csh.modules.user.dto.UserVo;
import com.csh.modules.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "鉴权")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "学生注册")
    @PostMapping("/register")
    public R<Long> register(@RequestBody @Valid RegisterReq req) {
        return R.ok(authService.register(req));
    }

    @Operation(summary = "登录获取 token")
    @PostMapping("/login")
    public R<LoginResp> login(@RequestBody @Valid LoginReq req) {
        return R.ok(authService.login(req));
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public R<UserVo> me() {
        return R.ok(authService.me());
    }

    @Operation(summary = "登出（前端清 token 即可，这里只是占位）")
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
