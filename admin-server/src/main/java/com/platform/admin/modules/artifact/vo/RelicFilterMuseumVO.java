package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelicFilterMuseumVO {
    private String objectId;
    private String name;
    private String nameCn;
}
