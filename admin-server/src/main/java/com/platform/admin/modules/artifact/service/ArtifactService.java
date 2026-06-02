package com.platform.admin.modules.artifact.service;

import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.vo.ArtifactDetailVO;
import com.platform.admin.modules.artifact.vo.ArtifactPageVO;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import org.springframework.web.multipart.MultipartFile;

public interface ArtifactService {
    ArtifactPageVO pageRelics(long page, long size, String keyword, String period, String type, String material, String museum, String sort);

    ArtifactDetailVO getRelicById(String objectId);

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

    /**
     * CSV 批量创建文物（PRD V1.3.1）：校验全部通过后单事务顺序插入，返回 {@code objectIds}。
     *
     * @param file multipart 字段 {@code file}，UTF-8 CSV
     * @return 与插入顺序一致的主键列表
     */
    RelicCsvImportResultVO importRelicsFromCsv(MultipartFile file);
}
