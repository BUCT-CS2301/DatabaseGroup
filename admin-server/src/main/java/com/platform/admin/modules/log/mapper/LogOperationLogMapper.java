package com.platform.admin.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.log.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogOperationLogMapper extends BaseMapper<OperationLogEntity> {
}
