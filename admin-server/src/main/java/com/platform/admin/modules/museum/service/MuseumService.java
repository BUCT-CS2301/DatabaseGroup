package com.platform.admin.modules.museum.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.museum.dto.CreateMuseumRequest;
import com.platform.admin.modules.museum.dto.UpdateMuseumRequest;
import com.platform.admin.modules.museum.vo.DeleteMuseumVO;
import com.platform.admin.modules.museum.vo.MuseumVO;

public interface MuseumService {
    PageResult<MuseumVO> pageMuseums(long page, long pageSize, String keyword);

    MuseumVO getMuseumById(String objectId);

    MuseumVO createMuseum(CreateMuseumRequest request);

    MuseumVO updateMuseum(String objectId, UpdateMuseumRequest request);

    DeleteMuseumVO deleteMuseum(String objectId);
}
