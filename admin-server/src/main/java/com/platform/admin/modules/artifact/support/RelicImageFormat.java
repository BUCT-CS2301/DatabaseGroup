package com.platform.admin.modules.artifact.support;

import java.util.Optional;

/**
 * 允许的文物栅格图格式（魔数识别 + 小写扩展名）。
 */
public enum RelicImageFormat {
    JPEG(".jpg"),
    PNG(".png"),
    GIF(".gif"),
    WEBP(".webp");

    private final String extension;

    RelicImageFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    /**
     * 根据文件头识别格式；不足字节时返回 empty。
     *
     * @param header 建议至少 12 字节
     */
    public static Optional<RelicImageFormat> detect(byte[] header) {
        if (header == null || header.length < 3) {
            return Optional.empty();
        }
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return Optional.of(JPEG);
        }
        if (header.length >= 8
                && header[0] == (byte) 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A) {
            return Optional.of(PNG);
        }
        if (header.length >= 6
                && header[0] == 'G'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == '8'
                && header[4] == '7'
                && header[5] == 'a') {
            return Optional.of(GIF);
        }
        if (header.length >= 6
                && header[0] == 'G'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == '8'
                && header[4] == '9'
                && header[5] == 'a') {
            return Optional.of(GIF);
        }
        if (header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }
}
