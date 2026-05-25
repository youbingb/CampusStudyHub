package com.csh.modules.report.service.impl;

import com.csh.common.BusinessException;
import com.csh.common.constants.NotificationType;
import com.csh.modules.notification.service.NotificationService;
import com.csh.modules.report.entity.CreditLog;
import com.csh.modules.report.mapper.CreditLogMapper;
import com.csh.modules.report.service.CreditService;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final SysUserMapper sysUserMapper;
    private final CreditLogMapper creditLogMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeCredit(Long userId, int delta, String reason, String relatedType, Long relatedId) {
        if (userId == null) throw new BusinessException("userId 不能为空");
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在: " + userId);

        int before = user.getCreditScore() == null ? 100 : user.getCreditScore();
        int after = Math.max(0, Math.min(100, before + delta));

        SysUser patch = new SysUser();
        patch.setId(userId);
        patch.setCreditScore(after);
        sysUserMapper.updateById(patch);

        CreditLog clog = new CreditLog();
        clog.setUserId(userId);
        clog.setDelta(delta);
        clog.setReason(reason);
        clog.setRelatedType(relatedType);
        clog.setRelatedId(relatedId);
        creditLogMapper.insert(clog);

        try {
            String title = "信誉分变更";
            String content = String.format("%s 信誉分 %+d（当前 %d 分）", reason, delta, after);
            notificationService.send(userId, NotificationType.CREDIT_CHANGED, title, content, relatedId);
        } catch (Exception ex) {
            log.warn("CREDIT_CHANGED 通知推送失败 userId={}，事务继续提交", userId, ex);
        }
        return after;
    }

    @Override
    public int getScore(Long userId) {
        if (userId == null) return 0;
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return 0;
        return user.getCreditScore() == null ? 100 : user.getCreditScore();
    }
}
