package com.platform.admin.modules.museum.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMuseumVO {
    private String objectId;
    private boolean deleted;
}
