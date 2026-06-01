package com.platform.admin.modules.artifact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.common.log.OperationLogWriter;
import com.platform.admin.common.util.ClientIpUtils;
import com.platform.admin.config.RelicAutoImageProperties;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.artifact.mapper.RelicAssembler;
import com.platform.admin.modules.artifact.mapper.RelicFilterMuseumRow;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.support.RelicCsvImportParser;
import com.platform.admin.modules.artifact.support.RelicImageFormat;
import com.platform.admin.modules.artifact.support.RelicImageStorage;
import com.platform.admin.modules.artifact.support.RelicImageUrlsResolver;
import com.platform.admin.modules.artifact.support.RelicPublicUrlBuilder;
import com.platform.admin.modules.artifact.support.RelicSort;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicFilterMuseumVO;
import com.platform.admin.modules.artifact.vo.RelicFiltersVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicRelatedVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class ArtifactServiceImpl implements ArtifactService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final long MAX_RELIC_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final int FILTER_OPTION_LIMIT = 500;
    private static final int RELATED_LIMIT = 10;
    private static final String MODULE_RELIC = "RELIC";

    private final SecurityUtil securityUtil;
    private final OperationLogWriter operationLogWriter;
    private final ArtifactMapper artifactMapper;
    private final RelicImageStorage relicImageStorage;
    private final RelicAutoImageProperties relicAutoImageProperties;
    private final RelicPublicUrlBuilder relicPublicUrlBuilder;
    private final RelicImageUrlsResolver relicImageUrlsResolver;
    private final RelicCsvImportParser relicCsvImportParser;
    private final TransactionTemplate transactionTemplate;

    public ArtifactServiceImpl(
            SecurityUtil securityUtil,
            OperationLogWriter operationLogWriter,
            ArtifactMapper artifactMapper,
            RelicImageStorage relicImageStorage,
            RelicAutoImageProperties relicAutoImageProperties,
            RelicPublicUrlBuilder relicPublicUrlBuilder,
            RelicImageUrlsResolver relicImageUrlsResolver,
            RelicCsvImportParser relicCsvImportParser,
            PlatformTransactionManager platformTransactionManager) {
        this.securityUtil = securityUtil;
        this.operationLogWriter = operationLogWriter;
        this.artifactMapper = artifactMapper;
        this.relicImageStorage = relicImageStorage;
        this.relicAutoImageProperties = relicAutoImageProperties;
        this.relicPublicUrlBuilder = relicPublicUrlBuilder;
        this.relicImageUrlsResolver = relicImageUrlsResolver;
        this.relicCsvImportParser = relicCsvImportParser;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Override
    public PageResult<RelicVO> pageRelics(
            long page,
            long pageSize,
            String keyword,
            String museumId,
            String period,
            String type,
            String material,
            String sort) {
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        RelicSort relicSort = RelicSort.parse(sort);

        LambdaQueryWrapper<ArtifactEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArtifactEntity::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ArtifactEntity::getTitle, keyword)
                    .or().like(ArtifactEntity::getAccessionNumber, keyword));
        }
        if (StringUtils.hasText(museumId)) {
            wrapper.eq(ArtifactEntity::getMuseumId, museumId);
        }
        if (StringUtils.hasText(period)) {
            wrapper.eq(ArtifactEntity::getPeriod, period);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(ArtifactEntity::getType, type);
        }
        if (StringUtils.hasText(material)) {
            wrapper.eq(ArtifactEntity::getMaterial, material);
        }
        applySort(wrapper, relicSort);

        Page<ArtifactEntity> mpPage = new Page<>(safePage, safePageSize);
        Page<ArtifactEntity> result = artifactMapper.selectPage(mpPage, wrapper);

        List<RelicVO> records = result.getRecords().stream().map(RelicAssembler::toVO).toList();
        log.info(
                "event=relic_browse_list_search page={} pageSize={} sort={} result_count={}",
                safePage,
                safePageSize,
                relicSort.queryValue(),
                result.getTotal());
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public RelicFiltersVO getRelicFilters() {
        List<String> periods = artifactMapper.selectDistinctPeriods(FILTER_OPTION_LIMIT);
        List<String> types = artifactMapper.selectDistinctTypes(FILTER_OPTION_LIMIT);
        List<String> materials = artifactMapper.selectDistinctMaterials(FILTER_OPTION_LIMIT);
        List<RelicFilterMuseumVO> museums = artifactMapper.selectMuseumsWithArtifacts(FILTER_OPTION_LIMIT).stream()
                .map(this::toFilterMuseum)
                .toList();
        log.info(
                "event=relic_browse_filters_load period_count={} type_count={} material_count={} museum_count={}",
                periods.size(),
                types.size(),
                materials.size(),
                museums.size());
        return RelicFiltersVO.builder()
                .periods(periods)
                .types(types)
                .materials(materials)
                .museums(museums)
                .build();
    }

    @Override
    public RelicVO getRelicById(String objectId) {
        ArtifactEntity entity = getNotDeletedById(objectId);
        List<String> imageUrls = relicImageUrlsResolver.resolve(entity);
        if (imageUrls.isEmpty()) {
            log.warn("relic detail has no resolvable images objectId={}", objectId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        String primary = imageUrls.get(0);
        log.info("event=relic_browse_detail_view object_id={} image_count={}", objectId, imageUrls.size());
        return RelicAssembler.toVO(entity, imageUrls, primary);
    }

    @Override
    public RelicRelatedVO getRelicRelated(String objectId) {
        ArtifactEntity current = getNotDeletedById(objectId);
        List<RelicVO> related = collectRelated(current, RELATED_LIMIT);
        String periodTag = StringUtils.hasText(current.getPeriod()) ? current.getPeriod() : null;
        log.info(
                "event=relic_browse_related_view object_id={} period_tag={} related_count={}",
                objectId,
                periodTag,
                related.size());
        return RelicRelatedVO.builder().periodTag(periodTag).related(related).build();
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
        writeOperationLog("CREATE", objectId, Map.of(
                "title", request.getTitle(),
                "museumId", request.getMuseumId()
        ));
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
        writeOperationLog("UPDATE", objectId, request);
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
        writeOperationLog("UPDATE", objectId, Map.of("action", "UPLOAD_IMAGE", "imagePath", imagePath));
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
        return relicPublicUrlBuilder.fromImagePath(imagePath);
    }

    private void applySort(LambdaQueryWrapper<ArtifactEntity> wrapper, RelicSort sort) {
        switch (sort) {
            case HOT -> wrapper.last(
                    "ORDER BY GREATEST(0, 1000 - DATEDIFF(CURDATE(), DATE(create_time))) DESC, create_time DESC");
            case NAME -> wrapper.orderByAsc(ArtifactEntity::getTitle);
            case PERIOD -> wrapper.last("ORDER BY (period IS NULL), period ASC, title ASC");
        }
    }

    private RelicFilterMuseumVO toFilterMuseum(RelicFilterMuseumRow row) {
        return RelicFilterMuseumVO.builder()
                .objectId(row.getObjectId())
                .name(row.getName())
                .nameCn(row.getNameCn())
                .build();
    }

    private List<RelicVO> collectRelated(ArtifactEntity current, int max) {
        Set<String> exclude = new HashSet<>();
        exclude.add(current.getObjectId());
        List<RelicVO> related = new ArrayList<>();
        if (StringUtils.hasText(current.getPeriod())) {
            appendRelated(related, exclude, max, w -> w.eq(ArtifactEntity::getPeriod, current.getPeriod()));
        }
        if (related.size() < max && StringUtils.hasText(current.getType())) {
            appendRelated(related, exclude, max, w -> w.eq(ArtifactEntity::getType, current.getType()));
        }
        if (related.size() < max && StringUtils.hasText(current.getMuseumId())) {
            appendRelated(related, exclude, max, w -> w.eq(ArtifactEntity::getMuseumId, current.getMuseumId()));
        }
        return related;
    }

    private void appendRelated(
            List<RelicVO> sink,
            Set<String> exclude,
            int max,
            Consumer<LambdaQueryWrapper<ArtifactEntity>> criteria) {
        int remaining = max - sink.size();
        if (remaining <= 0) {
            return;
        }
        LambdaQueryWrapper<ArtifactEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArtifactEntity::getIsDeleted, 0);
        if (!exclude.isEmpty()) {
            wrapper.notIn(ArtifactEntity::getObjectId, exclude);
        }
        criteria.accept(wrapper);
        wrapper.last(
                "ORDER BY GREATEST(0, 1000 - DATEDIFF(CURDATE(), DATE(create_time))) DESC LIMIT " + remaining);
        List<ArtifactEntity> batch = artifactMapper.selectList(wrapper);
        for (ArtifactEntity entity : batch) {
            exclude.add(entity.getObjectId());
            String imageUrl = relicPublicUrlBuilder.resolvePrimary(entity.getImageUrl(), entity.getImagePath());
            sink.add(RelicAssembler.toBrowseCard(entity, imageUrl));
            if (sink.size() >= max) {
                break;
            }
        }
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
        writeOperationLog("DELETE", objectId, Map.of("objectId", objectId));
        return new DeleteRelicVO(objectId, 1);
    }

    private void writeOperationLog(String action, String targetId, Object requestParams) {
        try {
            AuthUser operator = securityUtil.getCurrentUser();
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("result", "SUCCESS");
            detail.put("requestParams", requestParams);
            operationLogWriter.writeAsync(
                    operator.objectId(),
                    resolveClientIp(),
                    MODULE_RELIC,
                    action,
                    "RELIC",
                    targetId,
                    detail
            );
        } catch (Exception ex) {
            log.warn("operation_log write skipped for relic action={} targetId={}", action, targetId, ex);
        }
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return ClientIpUtils.resolve(request);
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
