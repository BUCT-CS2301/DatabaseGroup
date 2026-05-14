package com.platform.admin.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sensitive_word")
public class SensitiveWordEntity {

    @TableId(value = "object_id", type = IdType.ASSIGN_UUID)
    private String objectId;

    @TableField("word")
    private String word;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}