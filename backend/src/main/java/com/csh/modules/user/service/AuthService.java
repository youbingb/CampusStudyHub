package com.csh.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.common.constants.Role;
import com.csh.modules.user.dto.LoginReq;
import com.csh.modules.user.dto.LoginResp;
import com.csh.modules.user.dto.RegisterReq;
import com.csh.modules.user.dto.UserVo;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import com.csh.security.JwtUtil;
import com.csh.security.LoginUserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterReq req) {
        Long exists = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (req.getStudentNo() != null && !req.getStudentNo().isBlank()) {
            Long stuExists = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, req.getStudentNo()));
            if (stuExists != null && stuExists > 0) {
                throw new BusinessException("学号已被注册");
            }
        }

        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRealName(req.getRealName());
        u.setStudentNo(emptyToNull(req.getStudentNo()));
        u.setPhone(emptyToNull(req.getPhone()));
        u.setEmail(emptyToNull(req.getEmail()));
        u.setRole(Role.STUDENT);
        u.setCreditScore(100);
        u.setStatus(1);
        sysUserMapper.insert(u);
        return u.getId();
    }

    public LoginResp login(LoginReq req) {
        SysUser u = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (u == null || !passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (u.getStatus() != null && u.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generate(u.getId(), u.getUsername(), u.getRole().name());
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(UserVo.of(u));
        return resp;
    }

    public UserVo me() {
        Long uid = LoginUserHolder.requireCurrent().getId();
        SysUser u = sysUserMapper.selectById(uid);
        if (u == null) {
            throw new BusinessException(401, "用户不存在或已被删除");
        }
        return UserVo.of(u);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
