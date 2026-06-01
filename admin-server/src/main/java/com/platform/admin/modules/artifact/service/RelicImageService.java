package com.platform.admin.modules.artifact.service;

import com.platform.admin.modules.artifact.entity.ArtifactImageEntity;
import com.platform.admin.modules.artifact.support.RelicImageFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 文物图片：{@code artifact_image} 表与落盘目录的统一访问。
 */
public interface RelicImageService {

    /** 按 UUID v7 文件名升序返回该文物的全部公网 URL。 */
    List<String> listPublicUrls(String artifactId);

    /** 首张图 URL；无图时返回 {@code null}。 */
    String getPrimaryPublicUrl(String artifactId);

    /** 批量取每个文物的首张图 URL（无图的不包含在 map 中）。 */
    Map<String, String> getPrimaryPublicUrlByArtifactIds(Collection<String> artifactIds);

    /**
     * 落盘并写入 {@code artifact_image}。
     *
     * @return 新插入的行
     */
    ArtifactImageEntity storeImage(String artifactId, byte[] data, RelicImageFormat format);
}
