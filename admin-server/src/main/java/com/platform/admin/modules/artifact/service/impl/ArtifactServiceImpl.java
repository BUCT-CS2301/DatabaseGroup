package com.platform.admin.modules.artifact.service.impl;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.entity.ArtifactEntity;
import com.platform.admin.modules.artifact.mapper.ArtifactMapper;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ArtifactServiceImpl implements ArtifactService {
    private static final Logger log = LoggerFactory.getLogger(ArtifactServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;

    private final SecurityUtil securityUtil;
    private final Map<String, ArtifactEntity> store = new ConcurrentHashMap<>();

    public ArtifactServiceImpl(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @Override
    public PageResult<RelicVO> pageRelics(long page, long pageSize, String keyword, String museumId) {
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        List<RelicVO> filtered = store.values().stream()
                .filter(item -> item.getIsDeleted() == 0)
                .filter(item -> keyword == null || keyword.isBlank() || containsKeyword(item, keyword))
                .filter(item -> museumId == null || museumId.isBlank() || museumId.equals(item.getMuseumId()))
                .sorted(Comparator.comparing(ArtifactEntity::getCreateTime).reversed())
                .map(ArtifactMapper::toVO)
                .toList();

        int from = (int) ((safePage - 1) * safePageSize);
        int to = (int) Math.min(from + safePageSize, filtered.size());
        List<RelicVO> pageRecords = from >= filtered.size() ? List.of() : filtered.subList(from, to);
        log.info("event=data_relic_list_view page={} pageSize={} hasKeyword={} hasMuseumFilter={}",
                safePage, safePageSize, keyword != null && !keyword.isBlank(), museumId != null && !museumId.isBlank());
        return new PageResult<>(pageRecords, filtered.size(), safePage, safePageSize);
    }

    @Override
    public RelicVO getRelicById(String objectId) {
        ArtifactEntity entity = getNotDeletedById(objectId);
        log.info("event=data_relic_detail_view objectId={}", objectId);
        return ArtifactMapper.toVO(entity);
    }

    @Override
    public RelicVO createRelic(CreateRelicRequest request) {
        securityUtil.requireAdminWritePermission();
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
                .imageUrl(request.getImageUrl())
                .imagePath(request.getImagePath())
                .creditLine(request.getCreditLine())
                .accessionNumber(request.getAccessionNumber())
                .crawlDate(request.getCrawlDate())
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        store.put(objectId, entity);
        log.info("event=data_relic_create_success objectId={}", objectId);
        return ArtifactMapper.toVO(entity);
    }

    @Override
    public RelicVO updateRelic(String objectId, UpdateRelicRequest request) {
        securityUtil.requireAdminWritePermission();
        ArtifactEntity entity = getNotDeletedById(objectId);
        merge(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        store.put(objectId, entity);
        log.info("event=data_relic_update_success objectId={}", objectId);
        return ArtifactMapper.toVO(entity);
    }

    @Override
    public DeleteRelicVO deleteRelic(String objectId) {
        securityUtil.requireAdminWritePermission();
        ArtifactEntity entity = getNotDeletedById(objectId);
        entity.setIsDeleted(1);
        entity.setUpdateTime(LocalDateTime.now());
        store.put(objectId, entity);
        log.info("event=data_relic_delete_success objectId={}", objectId);
        return new DeleteRelicVO(objectId, 1);
    }

    private ArtifactEntity getNotDeletedById(String objectId) {
        ArtifactEntity entity = store.get(objectId);
        if (entity == null || entity.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entity;
    }

    private boolean containsKeyword(ArtifactEntity item, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        boolean inTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerKeyword);
        boolean inAccession = item.getAccessionNumber() != null
                && item.getAccessionNumber().toLowerCase().contains(lowerKeyword);
        return inTitle || inAccession;
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
