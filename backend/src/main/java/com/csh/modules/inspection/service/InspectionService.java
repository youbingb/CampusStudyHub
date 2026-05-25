package com.csh.modules.inspection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.BusinessException;
import com.csh.common.PageResult;
import com.csh.modules.inspection.dto.CreateInspectionReq;
import com.csh.modules.inspection.dto.InspectionQuery;
import com.csh.modules.inspection.dto.InspectionVo;
import com.csh.modules.inspection.entity.Inspection;
import com.csh.modules.inspection.mapper.InspectionMapper;
import com.csh.modules.room.entity.StudyRoom;
import com.csh.modules.room.mapper.StudyRoomMapper;
import com.csh.modules.room.service.SeatStatusService;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionMapper inspectionMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SysUserMapper sysUserMapper;
    private final SeatStatusService seatStatusService;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long inspectorId, CreateInspectionReq req) {
        StudyRoom room = studyRoomMapper.selectById(req.getRoomId());
        if (room == null) throw new BusinessException("自习室不存在");

        Inspection ins = new Inspection();
        ins.setRoomId(req.getRoomId());
        ins.setInspectorId(inspectorId);
        ins.setContent(req.getContent());
        List<Long> issues = req.getIssues() == null ? List.of() : req.getIssues();
        try {
            ins.setIssues(objectMapper.writeValueAsString(issues));
        } catch (Exception e) {
            throw new BusinessException("巡检 issues 序列化失败");
        }
        inspectionMapper.insert(ins);

        String reason = "巡检发现故障"
                + (req.getContent() == null || req.getContent().isBlank() ? "" : ": " + req.getContent());
        for (Long seatId : issues) {
            if (seatId == null) continue;
            try {
                seatStatusService.markFault(seatId, inspectorId, reason);
            } catch (Exception ex) {
                log.warn("巡检标记故障失败 inspectionId={} seatId={}", ins.getId(), seatId, ex);
                throw ex;
            }
        }
        return ins.getId();
    }

    public PageResult<InspectionVo> page(InspectionQuery q) {
        int pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int pageSize = q.getSize() == null || q.getSize() < 1 ? 10 : Math.min(q.getSize(), 100);
        Page<Inspection> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<Inspection> w = new LambdaQueryWrapper<Inspection>()
                .eq(q.getRoomId() != null, Inspection::getRoomId, q.getRoomId())
                .eq(q.getInspectorId() != null, Inspection::getInspectorId, q.getInspectorId())
                .ge(q.getFrom() != null, Inspection::getCreatedAt,
                        q.getFrom() == null ? null : q.getFrom().atStartOfDay())
                .le(q.getTo() != null, Inspection::getCreatedAt,
                        q.getTo() == null ? null : q.getTo().atTime(LocalTime.MAX))
                .orderByDesc(Inspection::getCreatedAt);

        Page<Inspection> result = inspectionMapper.selectPage(page, w);
        List<InspectionVo> vos = toVos(result.getRecords());
        PageResult<InspectionVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPages(result.getPages());
        pr.setCurrent(result.getCurrent());
        pr.setSize(result.getSize());
        pr.setRecords(vos);
        return pr;
    }

    public InspectionVo getById(Long id) {
        Inspection ins = inspectionMapper.selectById(id);
        if (ins == null) throw new BusinessException("巡检记录不存在");
        return toVos(List.of(ins)).get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Inspection ins = inspectionMapper.selectById(id);
        if (ins == null) throw new BusinessException("巡检记录不存在");
        inspectionMapper.deleteById(id);
    }

    private List<InspectionVo> toVos(List<Inspection> list) {
        if (list == null || list.isEmpty()) return List.of();

        Set<Long> roomIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (Inspection ins : list) {
            if (ins.getRoomId() != null) roomIds.add(ins.getRoomId());
            if (ins.getInspectorId() != null) userIds.add(ins.getInspectorId());
        }
        Map<Long, String> roomNameMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            for (StudyRoom r : studyRoomMapper.selectBatchIds(roomIds)) {
                roomNameMap.put(r.getId(), r.getName());
            }
        }
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
                String display = (u.getRealName() == null || u.getRealName().isBlank())
                        ? u.getUsername() : u.getRealName();
                userNameMap.put(u.getId(), display);
            }
        }

        List<InspectionVo> vos = new ArrayList<>(list.size());
        for (Inspection ins : list) {
            InspectionVo vo = new InspectionVo();
            vo.setId(ins.getId());
            vo.setRoomId(ins.getRoomId());
            vo.setRoomName(roomNameMap.get(ins.getRoomId()));
            vo.setInspectorId(ins.getInspectorId());
            vo.setInspectorName(userNameMap.get(ins.getInspectorId()));
            vo.setContent(ins.getContent());
            vo.setIssues(parseIssues(ins.getIssues()));
            vo.setCreatedAt(ins.getCreatedAt());
            vos.add(vo);
        }
        return vos;
    }

    private List<Long> parseIssues(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.warn("解析 inspection.issues 失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}
