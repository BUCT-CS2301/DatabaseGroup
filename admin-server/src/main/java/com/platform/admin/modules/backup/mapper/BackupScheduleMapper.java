package com.platform.admin.modules.backup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.admin.modules.backup.entity.BackupScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupScheduleMapper extends BaseMapper<BackupScheduleEntity> {
}
