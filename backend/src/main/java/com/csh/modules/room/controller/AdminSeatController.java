package com.csh.modules.room.controller;

import com.csh.common.R;
import com.csh.modules.room.dto.BatchCreateSeatReq;
import com.csh.modules.room.dto.CreateSeatReq;
import com.csh.modules.room.dto.MarkFaultReq;
import com.csh.modules.room.dto.SeatVo;
import com.csh.modules.room.dto.UpdateSeatReq;
import com.csh.modules.room.service.SeatService;
import com.csh.modules.room.service.SeatStatusService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "座位（管理端）")
@RestController
@RequestMapping("/api/admin/seats")
@RequiredArgsConstructor
public class AdminSeatController {

    private final SeatService seatService;
    private final SeatStatusService seatStatusService;

    @Operation(summary = "按房间列出座位")
    @GetMapping("/by-room/{roomId}")
    public R<List<SeatVo>> listByRoom(@PathVariable Long roomId) {
        return R.ok(seatService.listByRoom(roomId));
    }

    @Operation(summary = "新建单个座位")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateSeatReq req) {
        return R.ok(seatService.create(req));
    }

    @Operation(summary = "按 rows × cols 网格批量生成座位")
    @PostMapping("/batch")
    public R<Integer> batchCreate(@RequestBody @Valid BatchCreateSeatReq req) {
        return R.ok(seatService.batchCreate(req));
    }

    @Operation(summary = "更新座位")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody UpdateSeatReq req) {
        seatService.update(id, req);
        return R.ok();
    }

    @Operation(summary = "删除座位")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return R.ok();
    }

    @Operation(summary = "标记座位故障")
    @PostMapping("/{id}/fault")
    public R<Void> markFault(@PathVariable Long id, @RequestBody @Valid MarkFaultReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        seatStatusService.markFault(id, uid, req.getReason());
        return R.ok();
    }

    @Operation(summary = "解除座位故障")
    @PostMapping("/{id}/fault/clear")
    public R<Void> clearFault(@PathVariable Long id) {
        seatStatusService.clearFault(id);
        return R.ok();
    }

    @Operation(summary = "重算座位状态")
    @PostMapping("/{id}/refresh")
    public R<Void> refresh(@PathVariable Long id) {
        seatStatusService.refresh(id);
        return R.ok();
    }
}
