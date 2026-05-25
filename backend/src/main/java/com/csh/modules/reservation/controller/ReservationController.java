package com.csh.modules.reservation.controller;

import com.csh.common.PageResult;
import com.csh.common.R;
import com.csh.modules.reservation.dto.CreateReservationReq;
import com.csh.modules.reservation.dto.ReservationQuery;
import com.csh.modules.reservation.dto.ReservationVo;
import com.csh.modules.reservation.service.ReservationService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "预约（学生端）")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "创建预约")
    @PostMapping
    public R<Long> create(@RequestBody @Valid CreateReservationReq req) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(reservationService.create(uid, req));
    }

    @Operation(summary = "我的预约分页")
    @GetMapping("/mine")
    public R<PageResult<ReservationVo>> mine(@ModelAttribute ReservationQuery query) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        query.setUserId(uid);
        return R.ok(reservationService.page(query));
    }

    @Operation(summary = "预约详情")
    @GetMapping("/{id}")
    public R<ReservationVo> detail(@PathVariable Long id) {
        return R.ok(reservationService.detail(id));
    }

    @Operation(summary = "取消预约（仅限自己且状态为 BOOKED）")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        reservationService.cancel(uid, id);
        return R.ok();
    }

    @Operation(summary = "签到")
    @PostMapping("/{id}/check-in")
    public R<Void> checkIn(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        reservationService.checkIn(uid, id);
        return R.ok();
    }

    @Operation(summary = "签退")
    @PostMapping("/{id}/check-out")
    public R<Void> checkOut(@PathVariable Long id) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        reservationService.checkOut(uid, id);
        return R.ok();
    }
}
