package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteRelicVO {
    private String objectId;
    private Integer isDeleted;
}
