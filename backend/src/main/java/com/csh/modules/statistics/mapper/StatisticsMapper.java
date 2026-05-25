package com.csh.modules.statistics.mapper;

import com.csh.modules.statistics.dto.FaultVo;
import com.csh.modules.statistics.dto.OccupancyVo;
import com.csh.modules.statistics.dto.PopularHourVo;
import com.csh.modules.statistics.dto.UsageVo;
import com.csh.modules.statistics.dto.ViolationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StatisticsMapper {

    List<OccupancyVo> selectOccupancy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<UsageVo> selectUsage(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                              @Param("limit") int limit);

    List<PopularHourVo> selectPopularHours(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<ViolationVo> selectViolations(@Param("limit") int limit);

    List<FaultVo> selectFaults(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
