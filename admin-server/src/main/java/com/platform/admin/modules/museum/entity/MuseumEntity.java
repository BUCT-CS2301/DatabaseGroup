package com.platform.admin.modules.museum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("museum")
public class MuseumEntity {
    @TableId(value = "object_id", type = IdType.INPUT)
    private String objectId;

    private String name;

    @TableField("name_cn")
    private String nameCn;

    private String location;

    private String website;
}
