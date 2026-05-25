package com.csh.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.PageResult;
import com.csh.modules.system.dto.OperationLogQuery;
import com.csh.modules.system.dto.OperationLogVo;
import com.csh.modules.system.entity.OperationLog;
import com.csh.modules.system.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public PageResult<OperationLogVo> page(OperationLogQuery q) {
        int pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int pageSize = q.getSize() == null || q.getSize() < 1 ? 20 : Math.min(q.getSize(), 200);
        Page<OperationLog> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<OperationLog> w = new LambdaQueryWrapper<OperationLog>()
                .eq(q.getModule() != null && !q.getModule().isBlank(), OperationLog::getModule, q.getModule())
                .eq(q.getAction() != null && !q.getAction().isBlank(), OperationLog::getAction, q.getAction())
                .like(q.getUsername() != null && !q.getUsername().isBlank(),
                        OperationLog::getUsername, q.getUsername())
                .eq(q.getUserId() != null, OperationLog::getUserId, q.getUserId())
                .ge(q.getFrom() != null, OperationLog::getCreatedAt,
                        q.getFrom() == null ? null : q.getFrom().atStartOfDay())
                .le(q.getTo() != null, OperationLog::getCreatedAt,
                        q.getTo() == null ? null : q.getTo().atTime(LocalTime.MAX))
                .orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> result = operationLogMapper.selectPage(page, w);
        List<OperationLogVo> vos = new ArrayList<>(result.getRecords().size());
        for (OperationLog row : result.getRecords()) {
            OperationLogVo vo = new OperationLogVo();
            vo.setId(row.getId());
            vo.setUserId(row.getUserId());
            vo.setUsername(row.getUsername());
            vo.setModule(row.getModule());
            vo.setAction(row.getAction());
            vo.setTargetId(row.getTargetId());
            vo.setIp(row.getIp());
            vo.setUa(row.getUa());
            vo.setCreatedAt(row.getCreatedAt());
            vos.add(vo);
        }
        PageResult<OperationLogVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPages(result.getPages());
        pr.setCurrent(result.getCurrent());
        pr.setSize(result.getSize());
        pr.setRecords(vos);
        return pr;
    }
}
