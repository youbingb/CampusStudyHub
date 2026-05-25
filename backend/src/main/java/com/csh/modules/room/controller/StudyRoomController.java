package com.csh.modules.room.controller;

import com.csh.common.R;
import com.csh.modules.room.dto.RoomVo;
import com.csh.modules.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "自习室（学生端）")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final RoomService roomService;

    @Operation(summary = "列出开放的自习室")
    @GetMapping
    public R<List<RoomVo>> list() {
        return R.ok(roomService.listAll(true));
    }

    @Operation(summary = "自习室详情")
    @GetMapping("/{id}")
    public R<RoomVo> detail(@PathVariable Long id) {
        return R.ok(roomService.getDetail(id));
    }
}
