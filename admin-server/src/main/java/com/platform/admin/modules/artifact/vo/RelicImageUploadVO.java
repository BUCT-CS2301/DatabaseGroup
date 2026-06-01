package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelicImageUploadVO {
    private String objectId;
    /** 新落盘文件名（UUID v7 + 扩展名）。 */
    private String fileName;
    /** 与 {@code fileName} 对应的完整 HTTP(S) URL。 */
    private String imageUrl;
}
