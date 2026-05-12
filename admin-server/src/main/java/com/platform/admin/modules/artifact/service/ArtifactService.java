package com.platform.admin.modules.artifact.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicVO;

public interface ArtifactService {
    PageResult<RelicVO> pageRelics(long page, long pageSize, String keyword, String museumId);

    RelicVO getRelicById(String objectId);

    RelicVO createRelic(CreateRelicRequest request);

    RelicVO updateRelic(String objectId, UpdateRelicRequest request);

    DeleteRelicVO deleteRelic(String objectId);
}
