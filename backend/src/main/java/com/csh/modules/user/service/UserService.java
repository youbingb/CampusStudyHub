package com.csh.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.BusinessException;
import com.csh.common.PageResult;
import com.csh.modules.user.dto.ChangePasswordReq;
import com.csh.modules.user.dto.UpdateProfileReq;
import com.csh.modules.user.dto.UserQuery;
import com.csh.modules.user.dto.UserVo;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import com.csh.modules.report.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final CreditService creditService;

    @Transactional(rollbackFor = Exception.class)
    public UserVo updateProfile(Long userId, UpdateProfileReq req) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) throw new BusinessException(404, "用户不存在");
        if (req.getRealName() != null) u.setRealName(emptyToNull(req.getRealName()));
        if (req.getPhone() != null) u.setPhone(emptyToNull(req.getPhone()));
        if (req.getEmail() != null) u.setEmail(emptyToNull(req.getEmail()));
        sysUserMapper.updateById(u);
        return UserVo.of(u);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordReq req) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(req.getOldPassword(), u.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        sysUserMapper.updateById(u);
    }

    public PageResult<UserVo> adminList(UserQuery q) {
        Page<SysUser> p = new Page<>(q.getPage() == null ? 1 : q.getPage(),
                q.getSize() == null ? 10 : q.getSize());
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(q.getRole() != null, SysUser::getRole, q.getRole())
                .eq(q.getStatus() != null, SysUser::getStatus, q.getStatus())
                .and(q.getKeyword() != null && !q.getKeyword().isBlank(), wrapper -> wrapper
                        .like(SysUser::getUsername, q.getKeyword())
                        .or().like(SysUser::getRealName, q.getKeyword())
                        .or().like(SysUser::getStudentNo, q.getKeyword())
                        .or().like(SysUser::getPhone, q.getKeyword()))
                .orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> res = sysUserMapper.selectPage(p, w);
        PageResult<UserVo> r = new PageResult<>();
        r.setTotal(res.getTotal());
        r.setPages(res.getPages());
        r.setCurrent(res.getCurrent());
        r.setSize(res.getSize());
        r.setRecords(res.getRecords().stream().map(UserVo::of).toList());
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("status 只能是 0 或 1");
        }
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) throw new BusinessException(404, "用户不存在");
        u.setStatus(status);
        sysUserMapper.updateById(u);
    }

    /**
     * 管理员手动调信誉。委托给 C 的 CreditService 保证 credit_log + 通知一并落地。
     * 返回变更后的分数。
     */
    public int adjustCredit(Long targetUserId, Integer delta, String reason) {
        SysUser u = sysUserMapper.selectById(targetUserId);
        if (u == null) throw new BusinessException(404, "用户不存在");
        return creditService.changeCredit(targetUserId, delta, reason, "ADMIN_ADJUST", null);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
