package com.platform.admin.modules.artifact.service.impl;

import com.platform.admin.modules.artifact.entity.ArtifactImageEntity;
import com.platform.admin.modules.artifact.mapper.ArtifactImageMapper;
import com.platform.admin.modules.artifact.service.RelicImageService;
import com.platform.admin.modules.artifact.support.RelicImageFileNames;
import com.platform.admin.modules.artifact.support.RelicImageFormat;
import com.platform.admin.modules.artifact.support.RelicImageStorage;
import com.platform.admin.modules.artifact.support.RelicPublicUrlBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelicImageServiceImpl implements RelicImageService {

    private final ArtifactImageMapper artifactImageMapper;
    private final RelicImageStorage relicImageStorage;
    private final RelicPublicUrlBuilder relicPublicUrlBuilder;

    public RelicImageServiceImpl(
            ArtifactImageMapper artifactImageMapper,
            RelicImageStorage relicImageStorage,
            RelicPublicUrlBuilder relicPublicUrlBuilder) {
        this.artifactImageMapper = artifactImageMapper;
        this.relicImageStorage = relicImageStorage;
        this.relicPublicUrlBuilder = relicPublicUrlBuilder;
    }

    @Override
    public List<String> listPublicUrls(String artifactId) {
        return artifactImageMapper.selectByArtifactId(artifactId).stream()
                .map(row -> relicPublicUrlBuilder.fromFileName(row.getFileName()))
                .filter(StringUtils::hasText)
                .toList();
    }

    @Override
    public String getPrimaryPublicUrl(String artifactId) {
        List<ArtifactImageEntity> rows = artifactImageMapper.selectByArtifactId(artifactId);
        if (rows.isEmpty()) {
            return null;
        }
        return relicPublicUrlBuilder.fromFileName(rows.get(0).getFileName());
    }

    @Override
    public Map<String, String> getPrimaryPublicUrlByArtifactIds(Collection<String> artifactIds) {
        if (CollectionUtils.isEmpty(artifactIds)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (ArtifactImageEntity row : artifactImageMapper.selectByArtifactIds(artifactIds)) {
            result.putIfAbsent(
                    row.getArtifactId(), relicPublicUrlBuilder.fromFileName(row.getFileName()));
        }
        return result;
    }

    @Override
    public ArtifactImageEntity storeImage(String artifactId, byte[] data, RelicImageFormat format) {
        String fileName = RelicImageFileNames.newFileName(format.extension());
        Path target = relicImageStorage.getRoot().resolve(fileName);
        try {
            Files.write(target, data, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new com.platform.admin.common.BusinessException(
                    com.platform.admin.common.ErrorCode.INTERNAL_ERROR, "保存图片失败");
        }
        ArtifactImageEntity entity =
                ArtifactImageEntity.builder().fileName(fileName).artifactId(artifactId).build();
        artifactImageMapper.insert(entity);
        return entity;
    }
}
