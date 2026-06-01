package com.platform.admin.modules.artifact.support;

import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 组装详情多图 {@code imageUrls}：主图 + 磁盘上 {@code {objectId}} 与 {@code {objectId}-{2..9}} 附加图。
 */
@Component
public class RelicImageUrlsResolver {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Pattern ADDITIONAL_FILE = Pattern.compile("^.+-([2-9])\\.[A-Za-z0-9]+$");

    private final RelicImageStorage relicImageStorage;
    private final RelicPublicUrlBuilder relicPublicUrlBuilder;

    public RelicImageUrlsResolver(RelicImageStorage relicImageStorage, RelicPublicUrlBuilder relicPublicUrlBuilder) {
        this.relicImageStorage = relicImageStorage;
        this.relicPublicUrlBuilder = relicPublicUrlBuilder;
    }

    /**
     * 去重后按主图优先、附加序号升序返回；可能为空（调用方应 404）。
     */
    public List<String> resolve(ArtifactEntity entity) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        String primary = relicPublicUrlBuilder.resolvePrimary(entity.getImageUrl(), entity.getImagePath());
        if (StringUtils.hasText(primary)) {
            urls.add(primary);
        }
        collectFromDisk(entity.getObjectId(), urls);
        return new ArrayList<>(urls);
    }

    private void collectFromDisk(String objectId, LinkedHashSet<String> urls) {
        Path root = relicImageStorage.getRoot();
        if (!Files.isDirectory(root)) {
            return;
        }
        List<DiskImage> diskImages = new ArrayList<>();
        try (Stream<Path> stream = Files.list(root)) {
            stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> matchesObjectId(objectId, name))
                    .forEach(name -> diskImages.add(toDiskImage(objectId, name)));
        } catch (IOException ignored) {
            return;
        }
        diskImages.sort((a, b) -> {
            int seqCompare = Integer.compare(a.sequence(), b.sequence());
            if (seqCompare != 0) {
                return seqCompare;
            }
            return a.fileName().compareToIgnoreCase(b.fileName());
        });
        for (DiskImage image : diskImages) {
            String url = relicPublicUrlBuilder.fileNameToUrl(image.fileName());
            if (StringUtils.hasText(url)) {
                urls.add(url);
            }
        }
    }

    private static boolean matchesObjectId(String objectId, String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String ext = lower.substring(dot + 1);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return false;
        }
        String base = lower.substring(0, dot);
        if (base.equals(objectId.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return ADDITIONAL_FILE.matcher(lower).matches()
                && lower.startsWith(objectId.toLowerCase(Locale.ROOT) + "-");
    }

    private static DiskImage toDiskImage(String objectId, String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        String prefix = objectId.toLowerCase(Locale.ROOT);
        if (lower.startsWith(prefix + "-")) {
            int dash = lower.lastIndexOf('-');
            int dot = lower.lastIndexOf('.');
            try {
                int seq = Integer.parseInt(lower.substring(dash + 1, dot));
                return new DiskImage(fileName, seq);
            } catch (NumberFormatException ex) {
                return new DiskImage(fileName, 1);
            }
        }
        return new DiskImage(fileName, 1);
    }

    private record DiskImage(String fileName, int sequence) {
    }
}
