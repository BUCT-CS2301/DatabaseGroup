package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文物图片上传成功响应（PRD §2.4）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelicImageUploadVO {
    private String objectId;
    private String imagePath;
    /** 与落盘文件一致的对外 URL（与库表 {@code image_url} 对齐）。 */
    private String imageUrl;
}
