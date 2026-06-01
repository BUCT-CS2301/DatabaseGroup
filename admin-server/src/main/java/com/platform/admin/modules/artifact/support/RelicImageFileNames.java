package com.platform.admin.modules.artifact.support;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * 文物落盘文件名生成（UUID v7 + 扩展名）。
 */
public final class RelicImageFileNames {

    private RelicImageFileNames() {
    }

    /**
     * @param extensionWithDot 如 {@code .jpg}
     */
    public static String newFileName(String extensionWithDot) {
        String ext = extensionWithDot == null ? "" : extensionWithDot.strip();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        return UuidCreator.getTimeOrderedEpoch().toString() + ext.toLowerCase(java.util.Locale.ROOT);
    }
}
