package com.csh.modules.room.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.common.constants.SeatStatus;
import com.csh.modules.room.dto.BatchCreateSeatReq;
import com.csh.modules.room.dto.CreateSeatReq;
import com.csh.modules.room.dto.SeatVo;
import com.csh.modules.room.dto.UpdateSeatReq;
import com.csh.modules.room.entity.Seat;
import com.csh.modules.room.entity.StudyRoom;
import com.csh.modules.room.mapper.SeatMapper;
import com.csh.modules.room.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;

    public List<SeatVo> listByRoom(Long roomId) {
        if (studyRoomMapper.selectById(roomId) == null) {
            throw new BusinessException("自习室不存在");
        }
        List<Seat> seats = seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNo)
                .orderByAsc(Seat::getColNo));
        List<SeatVo> vos = new ArrayList<>(seats.size());
        for (Seat s : seats) {
            vos.add(toVo(s));
        }
        return vos;
    }

    public SeatVo getDetail(Long id) {
        Seat s = seatMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("座位不存在");
        }
        return toVo(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateSeatReq req) {
        if (studyRoomMapper.selectById(req.getRoomId()) == null) {
            throw new BusinessException("自习室不存在");
        }
        Long dup = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, req.getRoomId())
                .eq(Seat::getSeatNo, req.getSeatNo()));
        if (dup != null && dup > 0) {
            throw new BusinessException("该自习室下已存在同编号座位");
        }
        Seat s = new Seat();
        BeanUtils.copyProperties(req, s);
        s.setStatus(SeatStatus.AVAILABLE);
        seatMapper.insert(s);
        return s.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(BatchCreateSeatReq req) {
        StudyRoom room = studyRoomMapper.selectById(req.getRoomId());
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        String prefix = StringUtils.hasText(req.getPrefix()) ? req.getPrefix() : "A";
        int count = 0;
        for (int row = 1; row <= req.getRows(); row++) {
            for (int col = 1; col <= req.getCols(); col++) {
                String seatNo = String.format("%s%d-%02d", prefix, row, col);
                Long dup = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, req.getRoomId())
                        .eq(Seat::getSeatNo, seatNo));
                if (dup != null && dup > 0) {
                    continue;
                }
                Seat s = new Seat();
                s.setRoomId(req.getRoomId());
                s.setSeatNo(seatNo);
                s.setRowNo(row);
                s.setColNo(col);
                s.setStatus(SeatStatus.AVAILABLE);
                s.setFeature(req.getFeature());
                seatMapper.insert(s);
                count++;
            }
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateSeatReq req) {
        Seat s = seatMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("座位不存在");
        }
        if (StringUtils.hasText(req.getSeatNo())) {
            Long dup = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                    .eq(Seat::getRoomId, s.getRoomId())
                    .eq(Seat::getSeatNo, req.getSeatNo())
                    .ne(Seat::getId, id));
            if (dup != null && dup > 0) {
                throw new BusinessException("该自习室下已存在同编号座位");
            }
            s.setSeatNo(req.getSeatNo());
        }
        if (req.getRowNo() != null) s.setRowNo(req.getRowNo());
        if (req.getColNo() != null) s.setColNo(req.getColNo());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        if (req.getFeature() != null) s.setFeature(req.getFeature());
        seatMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Seat s = seatMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("座位不存在");
        }
        seatMapper.deleteById(id);
    }

    private SeatVo toVo(Seat s) {
        SeatVo vo = new SeatVo();
        BeanUtils.copyProperties(s, vo);
        return vo;
    }
}
