package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelicFiltersVO {
    private List<String> periods;
    private List<String> types;
    private List<String> materials;
    private List<RelicFilterMuseumVO> museums;
}
