package com.csh.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csh.modules.system.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
