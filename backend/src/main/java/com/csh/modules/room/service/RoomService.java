package com.csh.modules.room.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.common.constants.SeatStatus;
import com.csh.modules.room.dto.CreateRoomReq;
import com.csh.modules.room.dto.RoomVo;
import com.csh.modules.room.dto.UpdateRoomReq;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;

    public List<RoomVo> listAll(boolean onlyOpen) {
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<StudyRoom>()
                .orderByAsc(StudyRoom::getId);
        if (onlyOpen) {
            wrapper.eq(StudyRoom::getStatus, 1);
        }
        List<StudyRoom> rooms = studyRoomMapper.selectList(wrapper);
        if (rooms.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roomIds = rooms.stream().map(StudyRoom::getId).toList();
        List<Seat> seats = seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .in(Seat::getRoomId, roomIds));

        Map<Long, int[]> stat = new HashMap<>();
        for (Seat s : seats) {
            int[] arr = stat.computeIfAbsent(s.getRoomId(), k -> new int[2]);
            arr[0]++;
            if (s.getStatus() == SeatStatus.AVAILABLE) {
                arr[1]++;
            }
        }

        List<RoomVo> result = new ArrayList<>(rooms.size());
        for (StudyRoom r : rooms) {
            RoomVo vo = toVo(r);
            int[] arr = stat.getOrDefault(r.getId(), new int[2]);
            vo.setTotalSeats(arr[0]);
            vo.setAvailableSeats(arr[1]);
            result.add(vo);
        }
        return result;
    }

    public RoomVo getDetail(Long id) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        RoomVo vo = toVo(room);
        Long total = seatMapper.selectCount(new LambdaQueryWrapper<Seat>().eq(Seat::getRoomId, id));
        Long avail = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, id)
                .eq(Seat::getStatus, SeatStatus.AVAILABLE));
        vo.setTotalSeats(total == null ? 0 : total.intValue());
        vo.setAvailableSeats(avail == null ? 0 : avail.intValue());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateRoomReq req) {
        Long dup = studyRoomMapper.selectCount(new LambdaQueryWrapper<StudyRoom>()
                .eq(StudyRoom::getName, req.getName()));
        if (dup != null && dup > 0) {
            throw new BusinessException("已存在同名自习室");
        }
        StudyRoom room = new StudyRoom();
        BeanUtils.copyProperties(req, room);
        if (!StringUtils.hasText(room.getOpenTime())) room.setOpenTime("07:00");
        if (!StringUtils.hasText(room.getCloseTime())) room.setCloseTime("22:30");
        if (room.getCapacity() == null) room.setCapacity(0);
        room.setStatus(1);
        studyRoomMapper.insert(room);
        return room.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateRoomReq req) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        if (StringUtils.hasText(req.getName())) room.setName(req.getName());
        if (req.getLocation() != null) room.setLocation(req.getLocation());
        if (req.getCapacity() != null) room.setCapacity(req.getCapacity());
        if (StringUtils.hasText(req.getOpenTime())) room.setOpenTime(req.getOpenTime());
        if (StringUtils.hasText(req.getCloseTime())) room.setCloseTime(req.getCloseTime());
        if (req.getStatus() != null) room.setStatus(req.getStatus());
        if (req.getDescription() != null) room.setDescription(req.getDescription());
        studyRoomMapper.updateById(room);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        studyRoomMapper.deleteById(id);
    }

    private RoomVo toVo(StudyRoom r) {
        RoomVo vo = new RoomVo();
        BeanUtils.copyProperties(r, vo);
        return vo;
    }
}
