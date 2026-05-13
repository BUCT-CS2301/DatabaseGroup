package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CSV 批量创建文物成功响应（PRD V1.3.1），{@code objectIds} 与插入顺序一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelicCsvImportResultVO {
    private List<String> objectIds;
}
