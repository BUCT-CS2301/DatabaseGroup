package com.platform.admin.modules.museum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.PageResult;
import com.platform.admin.modules.museum.dto.CreateMuseumRequest;
import com.platform.admin.modules.museum.dto.UpdateMuseumRequest;
import com.platform.admin.modules.museum.entity.MuseumEntity;
import com.platform.admin.modules.museum.mapper.MuseumAssembler;
import com.platform.admin.modules.museum.mapper.MuseumMapper;
import com.platform.admin.modules.museum.service.MuseumService;
import com.platform.admin.modules.museum.vo.DeleteMuseumVO;
import com.platform.admin.modules.museum.vo.MuseumVO;
import com.platform.admin.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class MuseumServiceImpl implements MuseumService {
    private static final Logger log = LoggerFactory.getLogger(MuseumServiceImpl.class);
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;

    private final SecurityUtil securityUtil;
    private final MuseumMapper museumMapper;

    public MuseumServiceImpl(SecurityUtil securityUtil, MuseumMapper museumMapper) {
        this.securityUtil = securityUtil;
        this.museumMapper = museumMapper;
    }

    @Override
    public PageResult<MuseumVO> pageMuseums(long page, long pageSize, String keyword) {
        long safePage = page <= 0 ? DEFAULT_PAGE : page;
        long safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        LambdaQueryWrapper<MuseumEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MuseumEntity::getName, keyword)
                    .or().like(MuseumEntity::getNameCn, keyword));
        }
        wrapper.orderByAsc(MuseumEntity::getName);

        Page<MuseumEntity> mpPage = new Page<>(safePage, safePageSize);
        Page<MuseumEntity> result = museumMapper.selectPage(mpPage, wrapper);

        List<MuseumVO> records = result.getRecords().stream().map(MuseumAssembler::toVO).toList();
        log.info("event=data_museum_list_view page={} pageSize={} hasKeyword={}",
                safePage, safePageSize, StringUtils.hasText(keyword));
        return new PageResult<>(records, result.getTotal(), safePage, safePageSize);
    }

    @Override
    public MuseumVO getMuseumById(String objectId) {
        MuseumEntity entity = requireExisting(objectId);
        log.info("event=data_museum_detail_view objectId={}", objectId);
        return MuseumAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MuseumVO createMuseum(CreateMuseumRequest request) {
        securityUtil.requireAdminWritePermission();
        String objectId = UUID.randomUUID().toString();
        MuseumEntity entity = MuseumEntity.builder()
                .objectId(objectId)
                .name(request.getName())
                .nameCn(request.getNameCn())
                .location(request.getLocation())
                .website(request.getWebsite())
                .build();
        try {
            museumMapper.insert(entity);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据约束冲突，请检查字段内容");
        }
        log.info("event=data_museum_create_success objectId={}", objectId);
        return MuseumAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MuseumVO updateMuseum(String objectId, UpdateMuseumRequest request) {
        securityUtil.requireAdminWritePermission();
        MuseumEntity entity = requireExisting(objectId);
        merge(entity, request);
        try {
            museumMapper.updateById(entity);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据约束冲突，请检查字段内容");
        }
        log.info("event=data_museum_update_success objectId={}", objectId);
        return MuseumAssembler.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteMuseumVO deleteMuseum(String objectId) {
        securityUtil.requireAdminWritePermission();
        requireExisting(objectId);
        try {
            int rows = museumMapper.deleteById(objectId);
            if (rows == 0) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
            }
        } catch (DataIntegrityViolationException e) {
            log.warn("museum delete blocked by integrity: objectId={}", objectId, e);
            throw new BusinessException(ErrorCode.CONFLICT, "无法删除博物馆：仍存在强制关联数据");
        }
        log.info("event=data_museum_delete_success objectId={}", objectId);
        return new DeleteMuseumVO(objectId, true);
    }

    private MuseumEntity requireExisting(String objectId) {
        MuseumEntity entity = museumMapper.selectById(objectId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        return entity;
    }

    private void merge(MuseumEntity entity, UpdateMuseumRequest request) {
        if (request.getName() != null) {
            if (!StringUtils.hasText(request.getName())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "name不能为空");
            }
            entity.setName(request.getName());
        }
        if (request.getNameCn() != null) {
            entity.setNameCn(request.getNameCn());
        }
        if (request.getLocation() != null) {
            entity.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            entity.setWebsite(request.getWebsite());
        }
    }
}
