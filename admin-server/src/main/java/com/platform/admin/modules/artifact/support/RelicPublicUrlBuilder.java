package com.platform.admin.modules.artifact.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.config.RelicAutoImageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 将 {@code artifact_image.file_name} 转为对外可访问的 HTTP(S) URL。
 */
@Component
public class RelicPublicUrlBuilder {

    private final RelicAutoImageProperties relicAutoImageProperties;

    public RelicPublicUrlBuilder(RelicAutoImageProperties relicAutoImageProperties) {
        this.relicAutoImageProperties = relicAutoImageProperties;
    }

    /**
     * @param fileName 落盘文件名，如 {@code 01932a1b-....jpg}
     * @return 完整 URL；文件名为空时返回 {@code null}
     */
    public String fromFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        return fromRelativePath("relics-images/" + fileName.strip());
    }

    private String fromRelativePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        String base = requireBaseUrl();
        String trimmedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String path = relativePath.strip();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return trimmedBase + "/" + path;
    }

    private String requireBaseUrl() {
        String base = relicAutoImageProperties.getImagePublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR, "未配置 app.relics.image-public-base-url，无法生成图片 URL");
        }
        return base.strip();
    }
}
