package com.platform.admin.modules.artifact.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import org.springframework.web.multipart.MultipartFile;

public interface ArtifactService {
    PageResult<RelicVO> pageRelics(long page, long pageSize, String keyword, String museumId);

    RelicVO getRelicById(String objectId);

    RelicVO createRelic(CreateRelicRequest request);

    RelicVO updateRelic(String objectId, UpdateRelicRequest request);

    DeleteRelicVO deleteRelic(String objectId);

    /**
     * 管理员上传文物图片并落盘至 relics-images 目录（PRD）。
     *
     * @param objectId 文物主键，与路径参数一致
     * @param file     multipart 字段 {@code file}
     * @return 含 {@code imagePath}、{@code imageUrl} 的成功视图（与库表一致）
     */
    RelicImageUploadVO uploadRelicImage(String objectId, MultipartFile file);
}
