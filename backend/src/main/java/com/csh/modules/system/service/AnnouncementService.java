package com.csh.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.BusinessException;
import com.csh.common.PageResult;
import com.csh.modules.system.dto.AnnouncementQuery;
import com.csh.modules.system.dto.AnnouncementVo;
import com.csh.modules.system.dto.CreateAnnouncementReq;
import com.csh.modules.system.dto.UpdateAnnouncementReq;
import com.csh.modules.system.entity.Announcement;
import com.csh.modules.system.mapper.AnnouncementMapper;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final SysUserMapper sysUserMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long publisherId, CreateAnnouncementReq req) {
        Announcement a = new Announcement();
        a.setTitle(req.getTitle());
        a.setContent(req.getContent());
        a.setPublisherId(publisherId);
        boolean publish = Boolean.TRUE.equals(req.getPublishNow());
        a.setStatus(publish ? 1 : 0);
        a.setPublishedAt(publish ? LocalDateTime.now() : null);
        announcementMapper.insert(a);
        return a.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateAnnouncementReq req) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null) throw new BusinessException("公告不存在");
        Announcement patch = new Announcement();
        patch.setId(id);
        if (req.getTitle() != null) patch.setTitle(req.getTitle());
        if (req.getContent() != null) patch.setContent(req.getContent());
        announcementMapper.updateById(patch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        Announcement patch = new Announcement();
        patch.setId(id);
        patch.setStatus(1);
        patch.setPublishedAt(a.getPublishedAt() == null ? LocalDateTime.now() : a.getPublishedAt());
        announcementMapper.updateById(patch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        Announcement patch = new Announcement();
        patch.setId(id);
        patch.setStatus(0);
        announcementMapper.updateById(patch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        announcementMapper.deleteById(id);
    }

    public AnnouncementVo getById(Long id, boolean publishedOnly) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        if (publishedOnly && (a.getStatus() == null || a.getStatus() != 1)) {
            throw new BusinessException("公告不存在");
        }
        return toVos(List.of(a)).get(0);
    }

    public PageResult<AnnouncementVo> pageAdmin(AnnouncementQuery q) {
        return doPage(q, false);
    }

    public PageResult<AnnouncementVo> pageStudent(AnnouncementQuery q) {
        AnnouncementQuery copy = new AnnouncementQuery();
        copy.setKeyword(q.getKeyword());
        copy.setStatus(1);
        copy.setPage(q.getPage());
        copy.setSize(q.getSize());
        return doPage(copy, true);
    }

    public List<AnnouncementVo> listActive(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        Page<Announcement> page = new Page<>(1, n);
        Page<Announcement> r = announcementMapper.selectPage(page,
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getPublishedAt));
        return toVos(r.getRecords());
    }

    private PageResult<AnnouncementVo> doPage(AnnouncementQuery q, boolean studentMode) {
        int pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int pageSize = q.getSize() == null || q.getSize() < 1 ? 10 : Math.min(q.getSize(), 100);
        Page<Announcement> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<Announcement>()
                .eq(q.getStatus() != null, Announcement::getStatus, q.getStatus())
                .like(q.getKeyword() != null && !q.getKeyword().isBlank(),
                        Announcement::getTitle, q.getKeyword())
                .orderByDesc(studentMode ? Announcement::getPublishedAt : Announcement::getCreatedAt);

        Page<Announcement> result = announcementMapper.selectPage(page, w);
        PageResult<AnnouncementVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPages(result.getPages());
        pr.setCurrent(result.getCurrent());
        pr.setSize(result.getSize());
        pr.setRecords(toVos(result.getRecords()));
        return pr;
    }

    private List<AnnouncementVo> toVos(List<Announcement> list) {
        if (list == null || list.isEmpty()) return List.of();
        Set<Long> userIds = new HashSet<>();
        for (Announcement a : list) {
            if (a.getPublisherId() != null) userIds.add(a.getPublisherId());
        }
        Map<Long, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
                String display = (u.getRealName() == null || u.getRealName().isBlank())
                        ? u.getUsername() : u.getRealName();
                nameMap.put(u.getId(), display);
            }
        }

        List<AnnouncementVo> vos = new ArrayList<>(list.size());
        for (Announcement a : list) {
            AnnouncementVo vo = new AnnouncementVo();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setContent(a.getContent());
            vo.setPublisherId(a.getPublisherId());
            vo.setPublisherName(nameMap.get(a.getPublisherId()));
            vo.setStatus(a.getStatus());
            vo.setPublishedAt(a.getPublishedAt());
            vo.setCreatedAt(a.getCreatedAt());
            vo.setUpdatedAt(a.getUpdatedAt());
            vos.add(vo);
        }
        return vos;
    }
}
