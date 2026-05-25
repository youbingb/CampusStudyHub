package com.csh.modules.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.BusinessException;
import com.csh.common.PageResult;
import com.csh.common.constants.NotificationType;
import com.csh.common.constants.ReportStatus;
import com.csh.modules.notification.service.NotificationService;
import com.csh.modules.report.dto.CreateReportReq;
import com.csh.modules.report.dto.ProcessReportReq;
import com.csh.modules.report.dto.ReportQuery;
import com.csh.modules.report.dto.ReportVo;
import com.csh.modules.report.entity.Report;
import com.csh.modules.report.mapper.ReportMapper;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final SysUserMapper sysUserMapper;
    private final CreditService creditService;
    private final NotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long reporterId, CreateReportReq req) {
        if (reporterId == null) throw new BusinessException(401, "未登录");
        if (req.getTargetUserId() != null && req.getTargetUserId().equals(reporterId)) {
            throw new BusinessException("不能举报自己");
        }
        Report r = new Report();
        r.setReporterId(reporterId);
        r.setTargetUserId(req.getTargetUserId());
        r.setReservationId(req.getReservationId());
        r.setSeatId(req.getSeatId());
        r.setType(req.getType());
        r.setDescription(req.getDescription());
        r.setEvidenceUrl(req.getEvidenceUrl());
        r.setStatus(ReportStatus.PENDING);
        reportMapper.insert(r);

        try {
            notificationService.send(reporterId, NotificationType.REPORT_FILED,
                    "举报已提交",
                    "你的举报已收到，管理员将尽快处理。类型：" + req.getType(),
                    r.getId());
        } catch (Exception ex) {
            log.warn("举报提交通知推送失败 reportId={}", r.getId(), ex);
        }
        return r.getId();
    }

    public PageResult<ReportVo> pageMine(Long userId, ReportQuery q) {
        ReportQuery copy = new ReportQuery();
        copy.setStatus(q.getStatus());
        copy.setType(q.getType());
        copy.setKeyword(q.getKeyword());
        copy.setPage(q.getPage());
        copy.setSize(q.getSize());
        copy.setReporterId(userId);
        return pageAdmin(copy);
    }

    public PageResult<ReportVo> pageAdmin(ReportQuery q) {
        int pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int pageSize = q.getSize() == null || q.getSize() < 1 ? 10 : Math.min(q.getSize(), 100);
        Page<Report> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .eq(q.getStatus() != null, Report::getStatus, q.getStatus())
                .eq(q.getType() != null && !q.getType().isBlank(), Report::getType, q.getType())
                .eq(q.getReporterId() != null, Report::getReporterId, q.getReporterId())
                .eq(q.getTargetUserId() != null, Report::getTargetUserId, q.getTargetUserId())
                .like(q.getKeyword() != null && !q.getKeyword().isBlank(),
                        Report::getDescription, q.getKeyword())
                .orderByDesc(Report::getCreatedAt);

        Page<Report> result = reportMapper.selectPage(page, wrapper);
        List<ReportVo> vos = toVos(result.getRecords());
        PageResult<ReportVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPages(result.getPages());
        pr.setCurrent(result.getCurrent());
        pr.setSize(result.getSize());
        pr.setRecords(vos);
        return pr;
    }

    public ReportVo getById(Long id) {
        Report r = reportMapper.selectById(id);
        if (r == null) throw new BusinessException("举报不存在");
        return toVo(r, fetchUserNameMap(List.of(r)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long reportId) {
        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new BusinessException("举报不存在");
        if (!r.getReporterId().equals(userId)) throw new BusinessException(403, "无权撤销他人举报");
        if (r.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException("仅待处理的举报可撤销");
        }
        reportMapper.deleteById(reportId); // 软删
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(Long handlerId, Long reportId, ProcessReportReq req) {
        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new BusinessException("举报不存在");
        if (r.getStatus() != ReportStatus.PENDING && r.getStatus() != ReportStatus.PROCESSING) {
            throw new BusinessException("该举报已被处理: " + r.getStatus());
        }

        ReportStatus newStatus = req.getAction() == ProcessReportReq.Action.APPROVE
                ? ReportStatus.RESOLVED : ReportStatus.REJECTED;

        Report patch = new Report();
        patch.setId(reportId);
        patch.setStatus(newStatus);
        patch.setResult(req.getResult());
        patch.setHandlerId(handlerId);
        patch.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(patch);

        Integer delta = req.getCreditDelta();
        Long targetUserId = r.getTargetUserId();
        if (newStatus == ReportStatus.RESOLVED && delta != null && delta != 0 && targetUserId != null) {
            String reason = (req.getCreditReason() == null || req.getCreditReason().isBlank())
                    ? ("举报核实: " + r.getType()) : req.getCreditReason();
            try {
                creditService.changeCredit(targetUserId, delta, reason, "REPORT", reportId);
            } catch (Exception ex) {
                log.warn("举报核实信誉调整失败 reportId={} targetUserId={}", reportId, targetUserId, ex);
                throw ex;
            }
        }

        String title = newStatus == ReportStatus.RESOLVED ? "举报已核实" : "举报已驳回";
        String content = (req.getResult() == null || req.getResult().isBlank())
                ? title : req.getResult();
        try {
            notificationService.send(r.getReporterId(), NotificationType.REPORT_RESOLVED, title, content, reportId);
        } catch (Exception ex) {
            log.warn("举报处理通知推送失败 reportId={}", reportId, ex);
        }
    }

    private List<ReportVo> toVos(List<Report> reports) {
        if (reports == null || reports.isEmpty()) return List.of();
        Map<Long, String> nameMap = fetchUserNameMap(reports);
        return reports.stream().map(r -> toVo(r, nameMap)).toList();
    }

    private ReportVo toVo(Report r, Map<Long, String> nameMap) {
        ReportVo vo = new ReportVo();
        vo.setId(r.getId());
        vo.setType(r.getType());
        vo.setDescription(r.getDescription());
        vo.setEvidenceUrl(r.getEvidenceUrl());
        vo.setStatus(r.getStatus());
        vo.setResult(r.getResult());
        vo.setReporterId(r.getReporterId());
        vo.setReporterName(nameMap.get(r.getReporterId()));
        vo.setTargetUserId(r.getTargetUserId());
        vo.setTargetUserName(nameMap.get(r.getTargetUserId()));
        vo.setReservationId(r.getReservationId());
        vo.setSeatId(r.getSeatId());
        vo.setHandlerId(r.getHandlerId());
        vo.setHandlerName(nameMap.get(r.getHandlerId()));
        vo.setHandledAt(r.getHandledAt());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private Map<Long, String> fetchUserNameMap(List<Report> reports) {
        Set<Long> ids = new HashSet<>();
        for (Report r : reports) {
            if (r.getReporterId() != null) ids.add(r.getReporterId());
            if (r.getTargetUserId() != null) ids.add(r.getTargetUserId());
            if (r.getHandlerId() != null) ids.add(r.getHandlerId());
        }
        if (ids.isEmpty()) return Map.of();
        List<SysUser> users = sysUserMapper.selectBatchIds(ids);
        Map<Long, String> map = new HashMap<>();
        for (SysUser u : users) {
            String display = (u.getRealName() == null || u.getRealName().isBlank())
                    ? u.getUsername() : u.getRealName();
            map.put(u.getId(), display);
        }
        return map;
    }
}
