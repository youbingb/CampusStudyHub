package com.csh.modules.room.controller;

import com.csh.common.R;
import com.csh.modules.room.dto.SeatVo;
import com.csh.modules.room.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "座位（学生端）")
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "按房间列出座位（平面图）")
    @GetMapping("/by-room/{roomId}")
    public R<List<SeatVo>> listByRoom(@PathVariable Long roomId) {
        return R.ok(seatService.listByRoom(roomId));
    }

    @Operation(summary = "单座位详情")
    @GetMapping("/{id}")
    public R<SeatVo> detail(@PathVariable Long id) {
        return R.ok(seatService.getDetail(id));
    }
}
