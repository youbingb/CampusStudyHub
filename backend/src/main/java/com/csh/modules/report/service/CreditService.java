package com.csh.modules.report.service;

/**
 * 信誉分服务（Agent C owner）。契约见 docs/AGENTS.md §3.4。
 *
 * <p>跨模块依赖：Agent B 的 Reservation（违约扣分、签到加分）、Agent C 的 Report（核实奖惩）都调本服务。
 */
public interface CreditService {

    /**
     * 调整信誉分：写 credit_log + 改 sys_user.credit_score；发 CREDIT_CHANGED 站内通知。
     *
     * @param userId       目标用户
     * @param delta        变化量（正向加分，负向扣分）
     * @param reason       原因（人类可读，会出现在通知里）
     * @param relatedType  关联业务类型（RESERVATION / REPORT 等，可空）
     * @param relatedId    关联业务主键（可空）
     * @return 变更后的信誉分
     */
    int changeCredit(Long userId, int delta, String reason, String relatedType, Long relatedId);

    int getScore(Long userId);
}
