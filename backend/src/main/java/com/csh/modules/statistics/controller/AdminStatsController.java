package com.csh.modules.statistics.controller;

import com.csh.aop.OperationLog;
import com.csh.common.R;
import com.csh.modules.statistics.dto.FaultVo;
import com.csh.modules.statistics.dto.OccupancyVo;
import com.csh.modules.statistics.dto.PopularHourVo;
import com.csh.modules.statistics.dto.StatsQuery;
import com.csh.modules.statistics.dto.UsageVo;
import com.csh.modules.statistics.dto.ViolationVo;
import com.csh.modules.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "数据统计-管理")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "各自习室上座情况")
    @GetMapping("/occupancy")
    public R<List<OccupancyVo>> occupancy(StatsQuery query) {
        return R.ok(statisticsService.occupancy(query));
    }

    @Operation(summary = "用户使用排行 TopN")
    @GetMapping("/usage")
    public R<List<UsageVo>> usage(StatsQuery query) {
        return R.ok(statisticsService.usage(query));
    }

    @Operation(summary = "热门时段（按 start_time 的小时统计）")
    @GetMapping("/popular-hours")
    public R<List<PopularHourVo>> popularHours(StatsQuery query) {
        return R.ok(statisticsService.popularHours(query));
    }

    @Operation(summary = "违规扣分 TopN")
    @GetMapping("/violations")
    public R<List<ViolationVo>> violations(StatsQuery query) {
        return R.ok(statisticsService.violations(query));
    }

    @Operation(summary = "自习室故障汇总")
    @GetMapping("/faults")
    public R<List<FaultVo>> faults(StatsQuery query) {
        return R.ok(statisticsService.faults(query));
    }

    @Operation(summary = "导出 5 项统计（xlsx，多 Sheet）")
    @OperationLog(module = "统计", action = "导出 Excel")
    @GetMapping("/export")
    public void export(StatsQuery query, HttpServletResponse response) {
        statisticsService.export(query, response);
    }
}
