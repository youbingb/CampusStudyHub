package com.csh.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.modules.system.dto.RuleVo;
import com.csh.modules.system.dto.UpdateRuleReq;
import com.csh.modules.system.entity.ReservationRule;
import com.csh.modules.system.mapper.ReservationRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final ReservationRuleMapper ruleMapper;

    public RuleVo current() {
        ReservationRule rule = loadOrInit();
        return toVo(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public RuleVo update(UpdateRuleReq req) {
        ReservationRule rule = loadOrInit();
        if (req.getMaxDaily() != null) rule.setMaxDaily(req.getMaxDaily());
        if (req.getMaxAdvanceDays() != null) rule.setMaxAdvanceDays(req.getMaxAdvanceDays());
        if (req.getMinCredit() != null) rule.setMinCredit(req.getMinCredit());
        if (req.getCheckInGraceMin() != null) rule.setCheckInGraceMin(req.getCheckInGraceMin());
        if (req.getMaxDurationHours() != null) rule.setMaxDurationHours(req.getMaxDurationHours());
        if (req.getNoShowCreditPenalty() != null) rule.setNoShowCreditPenalty(req.getNoShowCreditPenalty());
        ruleMapper.updateById(rule);
        return toVo(rule);
    }

    private ReservationRule loadOrInit() {
        ReservationRule rule = ruleMapper.selectOne(
                new LambdaQueryWrapper<ReservationRule>().orderByAsc(ReservationRule::getId).last("limit 1"));
        if (rule != null) return rule;
        // 兜底：表为空时插入默认值（schema 与 data.sql 已预置 id=1，这里仅防御）
        rule = new ReservationRule();
        rule.setMaxDaily(2);
        rule.setMaxAdvanceDays(3);
        rule.setMinCredit(60);
        rule.setCheckInGraceMin(15);
        rule.setMaxDurationHours(4);
        rule.setNoShowCreditPenalty(5);
        ruleMapper.insert(rule);
        return rule;
    }

    private RuleVo toVo(ReservationRule r) {
        if (r == null) throw new BusinessException("预约规则未初始化");
        RuleVo vo = new RuleVo();
        vo.setId(r.getId());
        vo.setMaxDaily(r.getMaxDaily());
        vo.setMaxAdvanceDays(r.getMaxAdvanceDays());
        vo.setMinCredit(r.getMinCredit());
        vo.setCheckInGraceMin(r.getCheckInGraceMin());
        vo.setMaxDurationHours(r.getMaxDurationHours());
        vo.setNoShowCreditPenalty(r.getNoShowCreditPenalty());
        vo.setUpdatedAt(r.getUpdatedAt());
        return vo;
    }
}
