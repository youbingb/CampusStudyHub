package com.csh.modules.reservation.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.reservation.dto.CancelReservationReq;
import com.csh.modules.reservation.dto.ReservationQuery;
import com.csh.modules.reservation.dto.ReservationVo;
import com.csh.modules.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约（管理端）")
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "分页查询所有预约")
    @GetMapping
    public R<PageResult<ReservationVo>> list(@ModelAttribute ReservationQuery query) {
        return R.ok(reservationService.page(query));
    }

    @Operation(summary = "预约详情")
    @GetMapping("/{id}")
    public R<ReservationVo> detail(@PathVariable Long id) {
        return R.ok(reservationService.detail(id));
    }

    @Operation(summary = "管理员强制取消预约")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestBody @Valid CancelReservationReq req) {
        reservationService.adminCancel(id, req.getReason());
        return R.ok();
    }
}
