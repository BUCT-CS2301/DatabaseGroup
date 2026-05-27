package com.platform.admin.modules.backup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.backup.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
