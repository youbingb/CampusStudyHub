package com.csh.modules.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csh.modules.inspection.entity.Inspection;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InspectionMapper extends BaseMapper<Inspection> {
}
