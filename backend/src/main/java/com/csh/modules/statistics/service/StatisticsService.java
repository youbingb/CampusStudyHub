package com.csh.modules.statistics.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.csh.modules.statistics.dto.FaultVo;
import com.csh.modules.statistics.dto.OccupancyVo;
import com.csh.modules.statistics.dto.PopularHourVo;
import com.csh.modules.statistics.dto.StatsQuery;
import com.csh.modules.statistics.dto.UsageVo;
import com.csh.modules.statistics.dto.ViolationVo;
import com.csh.modules.statistics.mapper.StatisticsMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public List<OccupancyVo> occupancy(StatsQuery q) {
        return statisticsMapper.selectOccupancy(fromDateTime(q.getFrom()), toDateTime(q.getTo()));
    }

    public List<UsageVo> usage(StatsQuery q) {
        return statisticsMapper.selectUsage(fromDateTime(q.getFrom()), toDateTime(q.getTo()),
                topN(q.getTopN()));
    }

    public List<PopularHourVo> popularHours(StatsQuery q) {
        return statisticsMapper.selectPopularHours(fromDateTime(q.getFrom()), toDateTime(q.getTo()));
    }

    public List<ViolationVo> violations(StatsQuery q) {
        return statisticsMapper.selectViolations(topN(q.getTopN()));
    }

    public List<FaultVo> faults(StatsQuery q) {
        return statisticsMapper.selectFaults(fromDateTime(q.getFrom()), toDateTime(q.getTo()));
    }

    /**
     * 一键导出：5 张 sheet，使用 EasyExcel 4。
     */
    public void export(StatsQuery q, HttpServletResponse response) {
        try {
            String filename = URLEncoder.encode("statistics-" + LocalDate.now() + ".xlsx",
                    StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (OutputStream out = response.getOutputStream();
                 ExcelWriter writer = EasyExcel.write(out).build()) {
                WriteSheet s1 = EasyExcel.writerSheet(0, "自习室上座").head(OccupancyVo.class).build();
                writer.write(occupancy(q), s1);
                WriteSheet s2 = EasyExcel.writerSheet(1, "用户使用 TopN").head(UsageVo.class).build();
                writer.write(usage(q), s2);
                WriteSheet s3 = EasyExcel.writerSheet(2, "热门时段").head(PopularHourVo.class).build();
                writer.write(popularHours(q), s3);
                WriteSheet s4 = EasyExcel.writerSheet(3, "违规 TopN").head(ViolationVo.class).build();
                writer.write(violations(q), s4);
                WriteSheet s5 = EasyExcel.writerSheet(4, "自习室故障").head(FaultVo.class).build();
                writer.write(faults(q), s5);
            }
        } catch (Exception e) {
            log.error("导出统计 Excel 失败", e);
            throw new com.csh.common.BusinessException("导出失败: " + e.getMessage());
        }
    }

    private LocalDateTime fromDateTime(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }

    private LocalDateTime toDateTime(LocalDate d) {
        return d == null ? null : d.plusDays(1).atStartOfDay(); // 闭开区间
    }

    private int topN(Integer topN) {
        if (topN == null || topN < 1) return 10;
        return Math.min(topN, 200);
    }
}
