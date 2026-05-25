package com.csh.modules.user.controller;

import com.csh.common.R;
import com.csh.modules.user.dto.ChangePasswordReq;
import com.csh.modules.user.dto.UpdateProfileReq;
import com.csh.modules.user.dto.UserVo;
import com.csh.modules.user.service.UserService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户自助")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/me")
    public R<UserVo> updateMe(@RequestBody @Valid UpdateProfileReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(userService.updateProfile(uid, req));
    }

    @Operation(summary = "修改当前用户密码")
    @PostMapping("/me/password")
    public R<Void> changePassword(@RequestBody @Valid ChangePasswordReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        userService.changePassword(uid, req);
        return R.ok();
    }
}
