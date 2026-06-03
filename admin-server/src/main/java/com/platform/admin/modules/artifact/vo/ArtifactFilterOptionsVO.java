package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactFilterOptionsVO {

    private List<PeriodOptionVO> periods;

    private List<String> types;

    private List<String> materials;

    private List<MuseumOptionVO> museums;

    private List<Map<String, String>> sortOptions;
}