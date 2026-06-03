package com.platform.admin.modules.artifact.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.log.OperationLogWriter;
import com.platform.admin.common.util.ClientIpUtils;
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
import com.platform.admin.modules.artifact.vo.ArtifactDetailVO;
import com.platform.admin.modules.artifact.vo.ArtifactListItemVO;
import com.platform.admin.modules.artifact.vo.ArtifactPageVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import com.platform.admin.modules.museum.entity.MuseumEntity;
import com.platform.admin.modules.museum.mapper.MuseumMapper;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.platform.admin.modules.artifact.vo.ArtifactFilterOptionsVO;
import com.platform.admin.modules.artifact.vo.MuseumOptionVO;
import com.platform.admin.modules.artifact.vo.PeriodOptionVO;

import java.util.Comparator;
import java.util.Objects;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.platform.admin.modules.artifact.vo.ArtifactSearchItemVO;
import com.platform.admin.modules.artifact.vo.ArtifactSearchResultVO;

@Service
public class ArtifactServiceImpl implements ArtifactService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final long MAX_RELIC_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final String MODULE_RELIC = "RELIC";
    private static final String UNKNOWN_MUSEUM = "未知馆藏";
    private static final String DEFAULT_ACCESSION_NUMBER = "-";
    private static final String DEFAULT_LOCATION = "-";
    private static final List<String> SUPPORTED_SORTS = List.of("hot", "name", "period");

    private static final Map<String, Integer> PERIOD_ORDER_MAP = new LinkedHashMap<>();

static {
    PERIOD_ORDER_MAP.put("良渚文化", 1);
    PERIOD_ORDER_MAP.put("新石器时代", 1);
    PERIOD_ORDER_MAP.put("仰韶文化", 1);
    PERIOD_ORDER_MAP.put("夏", 2);
    PERIOD_ORDER_MAP.put("商", 3);
    PERIOD_ORDER_MAP.put("西周", 4);
    PERIOD_ORDER_MAP.put("春秋", 5);
    PERIOD_ORDER_MAP.put("战国", 6);
    PERIOD_ORDER_MAP.put("秦", 7);
    PERIOD_ORDER_MAP.put("西汉", 8);
    PERIOD_ORDER_MAP.put("东汉", 9);
    PERIOD_ORDER_MAP.put("三国", 10);
    PERIOD_ORDER_MAP.put("西晋", 11);
    PERIOD_ORDER_MAP.put("东晋", 12);
    PERIOD_ORDER_MAP.put("南北朝", 13);
    PERIOD_ORDER_MAP.put("隋", 14);
    PERIOD_ORDER_MAP.put("唐", 15);
    PERIOD_ORDER_MAP.put("五代十国", 16);
    PERIOD_ORDER_MAP.put("北宋", 17);
    PERIOD_ORDER_MAP.put("南宋", 18);
    PERIOD_ORDER_MAP.put("辽", 19);
    PERIOD_ORDER_MAP.put("金", 20);
    PERIOD_ORDER_MAP.put("元", 21);
    PERIOD_ORDER_MAP.put("明", 22);
    PERIOD_ORDER_MAP.put("清", 23);
    PERIOD_ORDER_MAP.put("民国", 24);
    PERIOD_ORDER_MAP.put("现代", 25);
}

    private static volatile boolean popularityTableMissingLogged = false;

    private final SecurityUtil securityUtil;
    private final OperationLogWriter operationLogWriter;
    private final ArtifactMapper artifactMapper;
    private final MuseumMapper museumMapper;
    private final RelicImageStorage relicImageStorage;
    private final RelicAutoImageProperties relicAutoImageProperties;
    private final RelicCsvImportParser relicCsvImportParser;
    private final TransactionTemplate transactionTemplate;

    public ArtifactServiceImpl(
            SecurityUtil securityUtil,
            OperationLogWriter operationLogWriter,
            ArtifactMapper artifactMapper,
            MuseumMapper museumMapper,
            RelicImageStorage relicImageStorage,
            RelicAutoImageProperties relicAutoImageProperties,
            RelicCsvImportParser relicCsvImportParser,
            PlatformTransactionManager platformTransactionManager) {
        this.securityUtil = securityUtil;
        this.operationLogWriter = operationLogWriter;
        this.artifactMapper = artifactMapper;
        this.museumMapper = museumMapper;
        this.relicImageStorage = relicImageStorage;
        this.relicAutoImageProperties = relicAutoImageProperties;
        this.relicCsvImportParser = relicCsvImportParser;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Override
    public ArtifactPageVO pageRelics(
            long page, long size, String keyword, String period, String type, String material, String museum, String sort) {
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        validateSort(sort);

        long offset = (safePage - 1) * safeSize;
        List<ArtifactEntity> entities = artifactMapper.selectPublicPage(
                offset, safeSize, keyword, period, type, material, museum, sort);
        long total = artifactMapper.countPublicPage(keyword, period, type, material, museum);

        List<ArtifactListItemVO> items = entities.stream().map(this::toListItemVO).toList();
        return ArtifactPageVO.builder()
                .items(items)
                .total(total)
                .page(safePage)
                .size(safeSize)
                .build();
    }

    @Override
    public ArtifactDetailVO getRelicById(String objectId) {
        ArtifactEntity entity = getNotDeletedById(objectId);
        MuseumEntity museumEntity = loadMuseum(entity.getMuseumId());
        List<String> imageUrls = resolveImageUrls(entity.getObjectId());
        String imageUrl = imageUrls.isEmpty() ? resolveDefaultImageUrl() : imageUrls.get(0);

        return ArtifactDetailVO.builder()
                .objectId(entity.getObjectId())
                .title(entity.getTitle())
                .period(entity.getPeriod())
                .type(entity.getType())
                .material(entity.getMaterial())
                .description(entity.getDescription())
                .dimensions(entity.getDimensions())
                .museum(resolveMuseumName(museumEntity))
                .location(resolveLocation(museumEntity))
                .detailUrl(entity.getDetailUrl())
                .imageUrl(imageUrl)
                .imageUrls(imageUrls)
                .creditLine(entity.getCreditLine())
                .accessionNumber(resolveAccessionNumber(entity.getAccessionNumber(), entity.getObjectId()))
                .popularity(calculatePopularity(entity.getObjectId()))
                .build();
    }

@Override
public ArtifactSearchResultVO searchArtifacts(
        String q,
        String keyword,
        String period,
        String type,
        String material,
        String museum,
        String sort,
        long page,
        long size) {
    String searchKeyword = resolveSearchKeyword(q, keyword);
    String safePeriod = trimToNull(period);
    String safeType = trimToNull(type);
    String safeMaterial = trimToNull(material);
    String safeMuseum = trimToNull(museum);
    String safeSort = trimToNull(sort);

    validateSort(safeSort);

    long safePage = page <= 0 ? DEFAULT_PAGE : page;
    long safeSize = size <= 0 ? 20L : Math.min(size, MAX_PAGE_SIZE);
    long offset = (safePage - 1) * safeSize;

    if ("period".equals(safeSort)) {
        List<ArtifactSearchItemVO> allItems = artifactMapper.searchArtifactsForPeriodSort(
                searchKeyword,
                safePeriod,
                safeType,
                safeMaterial,
                safeMuseum
        );

        List<ArtifactSearchItemVO> sortedItems = allItems.stream()
                .sorted(Comparator
                        .comparingInt((ArtifactSearchItemVO item) -> getPeriodOrder(item.getPeriod()))
                        .thenComparing(item -> item.getTitle() == null ? "" : item.getTitle())
                        .thenComparing(item -> item.getObjectId() == null ? "" : item.getObjectId()))
                .skip(offset)
                .limit(safeSize)
                .toList();

        return new ArtifactSearchResultVO(allItems.size(), safePage, safeSize, sortedItems);
    }

    long total = artifactMapper.countSearchArtifacts(
            searchKeyword,
            safePeriod,
            safeType,
            safeMaterial,
            safeMuseum
    );

    List<ArtifactSearchItemVO> items = total == 0
            ? List.of()
            : artifactMapper.searchArtifacts(
                    searchKeyword,
                    safePeriod,
                    safeType,
                    safeMaterial,
                    safeMuseum,
                    safeSort,
                    offset,
                    safeSize
            );

    return new ArtifactSearchResultVO(total, safePage, safeSize, items);
}

    @Override
public ArtifactFilterOptionsVO getArtifactFilterOptions() {
    List<PeriodOptionVO> periods = artifactMapper.selectDistinctPeriods().stream()
            .map(this::toPeriodOption)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                    PeriodOptionVO::getValue,
                    option -> option,
                    (left, right) -> left,
                    LinkedHashMap::new
            ))
            .values()
            .stream()
            .sorted(Comparator
                    .comparing(PeriodOptionVO::getOrder)
                    .thenComparing(PeriodOptionVO::getValue))
            .toList();

    List<String> types = artifactMapper.selectDistinctTypes();
    List<String> materials = artifactMapper.selectDistinctMaterials();
    List<MuseumOptionVO> museums = artifactMapper.selectMuseumOptions();

    List<Map<String, String>> sortOptions = List.of(
            Map.of("value", "default", "label", "默认"),
            Map.of("value", "name", "label", "名称"),
            Map.of("value", "period", "label", "年代")
    );

    return new ArtifactFilterOptionsVO(
            periods,
            types,
            materials,
            museums,
            sortOptions
    );
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
        artifactMapper.upsertArtifactImage(fileName, objectId);
        artifactMapper.deleteArtifactImagesExcept(objectId, fileName);
        artifactMapper.updateTimeById(objectId, LocalDateTime.now());
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

    private ArtifactListItemVO toListItemVO(ArtifactEntity entity) {
        MuseumEntity museumEntity = loadMuseum(entity.getMuseumId());
        List<String> imageUrls = resolveImageUrls(entity.getObjectId());
        String imageUrl = imageUrls.isEmpty() ? resolveDefaultImageUrl() : imageUrls.get(0);
        return ArtifactListItemVO.builder()
                .objectId(entity.getObjectId())
                .title(entity.getTitle())
                .period(entity.getPeriod())
                .type(entity.getType())
                .material(entity.getMaterial())
                .museum(resolveMuseumName(museumEntity))
                .imageUrl(imageUrl)
                .popularity(calculatePopularity(entity.getObjectId()))
                .build();
    }

    private MuseumEntity loadMuseum(String museumId) {
        if (!StringUtils.hasText(museumId)) {
            return null;
        }
        return museumMapper.selectById(museumId);
    }

    private String resolveMuseumName(MuseumEntity museumEntity) {
        if (museumEntity == null || !StringUtils.hasText(museumEntity.getName())) {
            log.warn("event=artifact_museum_missing");
            return UNKNOWN_MUSEUM;
        }
        return museumEntity.getName();
    }

    private String resolveLocation(MuseumEntity museumEntity) {
        if (museumEntity == null || !StringUtils.hasText(museumEntity.getLocation())) {
            return DEFAULT_LOCATION;
        }
        return museumEntity.getLocation();
    }

    private String resolveAccessionNumber(String accessionNumber, String objectId) {
        if (StringUtils.hasText(accessionNumber)) {
            return accessionNumber;
        }
        log.info("event=artifact_accession_number_fallback objectId={}", objectId);
        return DEFAULT_ACCESSION_NUMBER;
    }

    private List<String> resolveImageUrls(String artifactId) {
        List<String> fileNames = artifactMapper.selectImageFileNamesByArtifactId(artifactId);
        if (fileNames == null || fileNames.isEmpty()) {
            log.info("event=artifact_list_image_fallback artifactId={}", artifactId);
            return List.of(resolveDefaultImageUrl());
        }
        return fileNames.stream()
                .map(fileName -> buildRelicImageUrl("relics-images/" + fileName))
                .collect(Collectors.toList());
    }

    private String resolveDefaultImageUrl() {
        return buildRelicImageUrl("relics-images/default-placeholder.jpg");
    }

    private Integer calculatePopularity(String artifactId) {
        try {
            int viewCount = Math.max(artifactMapper.countViewsByArtifactId(artifactId), 0);
            int favoriteCount = Math.max(artifactMapper.countFavoritesByArtifactId(artifactId), 0);
            int value = (int) Math.floor(viewCount * 0.6 + favoriteCount * 0.4);
            return Math.max(value, 0);
        } catch (BadSqlGrammarException ex) {
            // 兼容未初始化 interaction 表的环境，按 PRD 降级策略返回 0。
            if (!popularityTableMissingLogged) {
                popularityTableMissingLogged = true;
                log.warn("event=artifact_hot_calculation_fallback fallback_reason=missing_interaction_tables");
            }
            return 0;
        }
    }

private void validateSort(String sort) {
    if (!StringUtils.hasText(sort)) {
        return;
    }
    if ("default".equals(sort)) {
        return;
    }
    if (SUPPORTED_SORTS.contains(sort)) {
        return;
    }
    throw new BusinessException(ErrorCode.BAD_REQUEST, "sort must be one of [default,hot,name,period]");
}

    private String resolveSearchKeyword(String q, String keyword) {
    if (StringUtils.hasText(q)) {
        return q.trim();
    }
    if (StringUtils.hasText(keyword)) {
        return keyword.trim();
    }
    return "";
}

private String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
        return null;
    }
    return value.trim();
}

private PeriodOptionVO toPeriodOption(String rawPeriod) {
    String periodName = matchPeriodName(rawPeriod);
    if (!StringUtils.hasText(periodName)) {
        return null;
    }
    Integer order = PERIOD_ORDER_MAP.get(periodName);
    if (order == null || order == 999) {
        return null;
    }
    return new PeriodOptionVO(periodName, periodName, order);
}

private String matchPeriodName(String period) {
    if (!StringUtils.hasText(period)) {
        return null;
    }

    String normalized = period.trim();

    if ("unknown".equalsIgnoreCase(normalized)
            || normalized.contains("未知")
            || normalized.contains("不详")) {
        return null;
    }

    List<String> keys = new ArrayList<>(PERIOD_ORDER_MAP.keySet());
    keys.sort((a, b) -> b.length() - a.length());

    for (String key : keys) {
        if (normalized.contains(key)) {
            return key;
        }
    }

    return null;
}

private int getPeriodOrder(String period) {
    String periodName = matchPeriodName(period);
    if (!StringUtils.hasText(periodName)) {
        return 999;
    }
    return PERIOD_ORDER_MAP.getOrDefault(periodName, 999);
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
