package com.platform.admin.modules.artifact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.config.RelicAutoImageProperties;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.artifact.mapper.RelicAssembler;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.support.RelicCsvImportParser;
import com.platform.admin.modules.artifact.support.RelicImageFormat;
import com.platform.admin.modules.artifact.support.RelicImageStorage;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ArtifactServiceImpl implements ArtifactService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final long MAX_RELIC_IMAGE_BYTES = 10L * 1024 * 1024;

    private final SecurityUtil securityUtil;
    private final ArtifactMapper artifactMapper;
    private final RelicImageStorage relicImageStorage;
    private final RelicAutoImageProperties relicAutoImageProperties;
    private final RelicCsvImportParser relicCsvImportParser;
    private final TransactionTemplate transactionTemplate;

    public ArtifactServiceImpl(
            SecurityUtil securityUtil,
            ArtifactMapper artifactMapper,
            RelicImageStorage relicImageStorage,
            RelicAutoImageProperties relicAutoImageProperties,
            RelicCsvImportParser relicCsvImportParser,
            PlatformTransactionManager platformTransactionManager) {
        this.securityUtil = securityUtil;
        this.artifactMapper = artifactMapper;
        this.relicImageStorage = relicImageStorage;
        this.relicAutoImageProperties = relicAutoImageProperties;
        this.relicCsvImportParser = relicCsvImportParser;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Override
    public PageResult<RelicVO> pageRelics(long page, long pageSize, String keyword, String museumId) {
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        LambdaQueryWrapper<ArtifactEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArtifactEntity::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ArtifactEntity::getTitle, keyword)
                    .or().like(ArtifactEntity::getAccessionNumber, keyword));
        }
        if (StringUtils.hasText(museumId)) {
            wrapper.eq(ArtifactEntity::getMuseumId, museumId);
        }
        wrapper.orderByDesc(ArtifactEntity::getCreateTime);

        Page<ArtifactEntity> mpPage = new Page<>(safePage, safePageSize);
        Page<ArtifactEntity> result = artifactMapper.selectPage(mpPage, wrapper);

        List<RelicVO> records = result.getRecords().stream().map(RelicAssembler::toVO).toList();
        log.info("event=data_relic_list_view page={} pageSize={} hasKeyword={} hasMuseumFilter={}",
                safePage, safePageSize, StringUtils.hasText(keyword), StringUtils.hasText(museumId));
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public RelicVO getRelicById(String objectId) {
        ArtifactEntity entity = getNotDeletedById(objectId);
        log.info("event=data_relic_detail_view objectId={}", objectId);
        return RelicAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RelicVO createRelic(CreateRelicRequest request) {
        securityUtil.requireAdminWritePermission();
        return insertNewRelic(request);
    }

    @Override
    public RelicCsvImportResultVO importRelicsFromCsv(MultipartFile file) {
        securityUtil.requireAdminWritePermission();
        log.info("event=relic_csv_import_start file_size_bytes={}", file.getSize());
        List<CreateRelicRequest> rows = relicCsvImportParser.parseAndValidate(file);
        return transactionTemplate.execute(status -> {
            List<String> objectIds = new ArrayList<>(rows.size());
            for (CreateRelicRequest row : rows) {
                objectIds.add(insertNewRelic(row).getObjectId());
            }
            log.info("event=relic_csv_import_success row_count={}", objectIds.size());
            return RelicCsvImportResultVO.builder().objectIds(objectIds).build();
        });
    }

    /**
     * 插入一条文物（与单条 POST 相同业务规则，含 M6 图片字段）；不含鉴权，调用方须已校验管理员权限。
     */
    private RelicVO insertNewRelic(CreateRelicRequest request) {
        if (request.getCrawlDate() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "crawlDate不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        String objectId = UUID.randomUUID().toString();
        String defaultExt = resolvedDefaultImageExtension();
        String imagePath = buildRelicImagePath(objectId, defaultExt);
        String imageUrl = buildRelicImageUrl(imagePath);
        ArtifactEntity entity = ArtifactEntity.builder()
                .objectId(objectId)
                .title(request.getTitle())
                .period(request.getPeriod())
                .type(request.getType())
                .material(request.getMaterial())
                .description(request.getDescription())
                .dimensions(request.getDimensions())
                .museumId(request.getMuseumId())
                .detailUrl(request.getDetailUrl())
                .imageUrl(imageUrl)
                .imagePath(imagePath)
                .creditLine(request.getCreditLine())
                .accessionNumber(request.getAccessionNumber())
                .crawlDate(request.getCrawlDate())
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        artifactMapper.insert(entity);
        log.info("event=data_relic_create_success objectId={}", objectId);
        return RelicAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RelicVO updateRelic(String objectId, UpdateRelicRequest request) {
        securityUtil.requireAdminWritePermission();
        ArtifactEntity entity = getNotDeletedById(objectId);
        merge(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        artifactMapper.updateById(entity);
        log.info("event=data_relic_update_success objectId={}", objectId);
        return RelicAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RelicImageUploadVO uploadRelicImage(String objectId, MultipartFile file) {
        securityUtil.requireAdminWritePermission();
        ArtifactEntity entity = getNotDeletedById(objectId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少图片文件或文件为空");
        }
        if (file.getSize() > MAX_RELIC_IMAGE_BYTES) {
            throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE, "单张图片不能超过 10 MB");
        }
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            log.warn("event=relic_image_upload_fail object_id={} error_code=read_failed http_status=500", objectId);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取上传文件失败");
        }
        if (data.length == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片文件不能为空");
        }
        byte[] header = data.length >= 12 ? Arrays.copyOf(data, 12) : Arrays.copyOf(data, data.length);
        Optional<RelicImageFormat> formatOpt = RelicImageFormat.detect(header);
        if (formatOpt.isEmpty()) {
            formatOpt = detectFormatWithImageIo(data);
        }
        if (formatOpt.isEmpty()) {
            log.warn(
                    "event=relic_image_upload_fail object_id={} error_code=unsupported_format http_status=400",
                    objectId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的图片格式");
        }
        RelicImageFormat format = formatOpt.get();
        String extWithDot = format.extension();
        Path root = relicImageStorage.getRoot();
        String fileName = objectId + extWithDot;
        Path target = root.resolve(fileName);
        try {
            Files.write(target, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            deleteStaleRelicImages(root, objectId, fileName);
        } catch (IOException e) {
            log.warn(
                    "event=relic_image_upload_fail object_id={} error_code=io_error http_status=500",
                    objectId,
                    e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "保存图片失败");
        }
        String imagePath = "relics-images/" + fileName;
        String imageUrl = buildRelicImageUrl(imagePath);
        entity.setImagePath(imagePath);
        entity.setImageUrl(imageUrl);
        entity.setUpdateTime(LocalDateTime.now());
        artifactMapper.updateById(entity);
        log.info(
                "event=relic_image_upload_success object_id={} file_ext={} file_size_bytes={}",
                objectId,
                extWithDot.substring(1),
                data.length);
        return RelicImageUploadVO.builder()
                .objectId(objectId)
                .imagePath(imagePath)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 配置中的默认扩展名，小写、不含点；用于创建文物时的预期文件名。
     */
    private String resolvedDefaultImageExtension() {
        String ext = relicAutoImageProperties.getDefaultImageExtension();
        if (!StringUtils.hasText(ext)) {
            return "jpg";
        }
        ext = ext.strip().toLowerCase(Locale.ROOT);
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        if (!ext.matches("[a-z0-9]{1,10}")) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "app.relics.default-image-extension 配置非法");
        }
        return ext;
    }

    private String buildRelicImagePath(String objectId, String extensionWithoutDot) {
        return "relics-images/" + objectId + "." + extensionWithoutDot;
    }

    /**
     * 将 {@code imagePublicBaseUrl} 与相对 {@code imagePath} 拼接为完整 URL（PRD 约定一种实现，见 application.yml）。
     */
    private String buildRelicImageUrl(String imagePath) {
        String base = relicAutoImageProperties.getImagePublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR, "未配置 app.relics.image-public-base-url，无法生成 imageUrl");
        }
        String trimmed = base.strip();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "/" + imagePath;
    }

    /**
     * 魔数未命中时，用 ImageIO 解码校验常见栅格图（满足 PRD「Content-Type 与/或魔数」之一；WebP 仍依赖魔数）。
     */
    private Optional<RelicImageFormat> detectFormatWithImageIo(byte[] data) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            if (iis == null) {
                return Optional.empty();
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return Optional.empty();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                String name = reader.getFormatName() == null ? "" : reader.getFormatName().toLowerCase(Locale.ROOT);
                if (name.contains("jpeg") || name.contains("jpg")) {
                    return Optional.of(RelicImageFormat.JPEG);
                }
                if (name.contains("png")) {
                    return Optional.of(RelicImageFormat.PNG);
                }
                if (name.contains("gif")) {
                    return Optional.of(RelicImageFormat.GIF);
                }
                return Optional.empty();
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * 同一文物主键下仅保留当前扩展名文件，删除其它后缀的旧图（PRD P1 推荐，避免磁盘残留）。
     */
    private void deleteStaleRelicImages(Path root, String objectId, String keepFileName) {
        String prefix = objectId + ".";
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> stream = Files.list(root)) {
            stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(prefix) && !name.equals(keepFileName);
                    })
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ex) {
                            log.warn("failed to delete stale relic image path={}", p.getFileName());
                        }
                    });
        } catch (IOException e) {
            log.warn("failed to list relic images for cleanup objectId={}", objectId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteRelicVO deleteRelic(String objectId) {
        securityUtil.requireAdminWritePermission();
        getNotDeletedById(objectId);
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<ArtifactEntity> uw = new LambdaUpdateWrapper<>();
        uw.eq(ArtifactEntity::getObjectId, objectId)
                .eq(ArtifactEntity::getIsDeleted, 0)
                .set(ArtifactEntity::getIsDeleted, 1)
                .set(ArtifactEntity::getUpdateTime, now);
        int rows = artifactMapper.update(null, uw);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        log.info("event=data_relic_delete_success objectId={}", objectId);
        return new DeleteRelicVO(objectId, 1);
    }

    private ArtifactEntity getNotDeletedById(String objectId) {
        ArtifactEntity entity = artifactMapper.selectById(objectId);
        if (entity == null || entity.getIsDeleted() == null || entity.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entity;
    }

    private void merge(ArtifactEntity entity, UpdateRelicRequest request) {
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getPeriod() != null) {
            entity.setPeriod(request.getPeriod());
        }
        if (request.getType() != null) {
            entity.setType(request.getType());
        }
        if (request.getMaterial() != null) {
            entity.setMaterial(request.getMaterial());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getDimensions() != null) {
            entity.setDimensions(request.getDimensions());
        }
        if (request.getMuseumId() != null) {
            entity.setMuseumId(request.getMuseumId());
        }
        if (request.getDetailUrl() != null) {
            entity.setDetailUrl(request.getDetailUrl());
        }
        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }
        if (request.getImagePath() != null) {
            entity.setImagePath(request.getImagePath());
        }
        if (request.getCreditLine() != null) {
            entity.setCreditLine(request.getCreditLine());
        }
        if (request.getAccessionNumber() != null) {
            entity.setAccessionNumber(request.getAccessionNumber());
        }
        if (request.getCrawlDate() != null) {
            entity.setCrawlDate(request.getCrawlDate());
        }
    }
}
