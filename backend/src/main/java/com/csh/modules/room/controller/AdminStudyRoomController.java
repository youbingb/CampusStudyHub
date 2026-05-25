package com.csh.modules.room.controller;

import com.csh.common.R;
import com.csh.modules.room.dto.CreateRoomReq;
import com.csh.modules.room.dto.RoomVo;
import com.csh.modules.room.dto.UpdateRoomReq;
import com.csh.modules.room.service.RoomService;
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

@Tag(name = "自习室（管理端）")
@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class AdminStudyRoomController {

    private final RoomService roomService;

    @Operation(summary = "列出全部自习室（含已关闭）")
    @GetMapping
    public R<List<RoomVo>> list() {
        return R.ok(roomService.listAll(false));
    }

    @Operation(summary = "新建自习室")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateRoomReq req) {
        return R.ok(roomService.create(req));
    }

    @Operation(summary = "更新自习室")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody UpdateRoomReq req) {
        roomService.update(id, req);
        return R.ok();
    }

    @Operation(summary = "删除自习室（软删）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return R.ok();
    }
}
