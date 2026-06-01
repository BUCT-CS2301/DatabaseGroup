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
public class RelicRelatedVO {
    private String periodTag;
    private List<RelicVO> related;
}
