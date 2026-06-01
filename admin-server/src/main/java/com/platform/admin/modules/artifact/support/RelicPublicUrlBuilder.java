package com.platform.admin.modules.artifact.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.config.RelicAutoImageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 将库内相对 {@code imagePath} 或已有完整 URL 转为对外可访问的 HTTP(S) 地址。
 */
@Component
public class RelicPublicUrlBuilder {

    private final RelicAutoImageProperties relicAutoImageProperties;

    public RelicPublicUrlBuilder(RelicAutoImageProperties relicAutoImageProperties) {
        this.relicAutoImageProperties = relicAutoImageProperties;
    }

    /**
     * 由 {@code relics-images/...} 相对路径拼接公网基址。
     *
     * @param imagePath 形如 relics-images/{objectId}.jpg
     * @return 完整 URL
     */
    public String fromImagePath(String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return null;
        }
        String base = requireBaseUrl();
        String trimmedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String path = imagePath.strip();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return trimmedBase + "/" + path;
    }

    /**
     * 优先使用已是 HTTP(S) 的 {@code imageUrl}，否则按 {@code imagePath} 拼接。
     */
    public String resolvePrimary(String imageUrl, String imagePath) {
        if (StringUtils.hasText(imageUrl)) {
            String trimmed = imageUrl.strip();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return trimmed.length() > 1000 ? trimmed.substring(0, 1000) : trimmed;
            }
        }
        return fromImagePath(imagePath);
    }

    public String fileNameToUrl(String fileName) {
        return fromImagePath("relics-images/" + fileName);
    }

    private String requireBaseUrl() {
        String base = relicAutoImageProperties.getImagePublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR, "未配置 app.relics.image-public-base-url，无法生成 imageUrl");
        }
        return base.strip();
    }
}
