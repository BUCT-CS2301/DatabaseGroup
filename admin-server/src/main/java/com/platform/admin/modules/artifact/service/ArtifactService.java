package com.platform.admin.modules.artifact.service;

import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.vo.ArtifactDetailVO;
import com.platform.admin.modules.artifact.vo.ArtifactFilterOptionsVO;
import com.platform.admin.modules.artifact.vo.ArtifactPageVO;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import org.springframework.web.multipart.MultipartFile;
import com.platform.admin.modules.artifact.vo.ArtifactSearchResultVO;

public interface ArtifactService {
    ArtifactPageVO pageRelics(long page, long size, String keyword, String period, String type, String material, String museum, String sort);

    ArtifactDetailVO getRelicById(String objectId);

    ArtifactSearchResultVO searchArtifacts(
            String q,
            String keyword,
            String period,
            String type,
            String material,
            String museum,
            String sort,
            long page,
            long size
    );

    ArtifactFilterOptionsVO getArtifactFilterOptions();

    RelicVO createRelic(CreateRelicRequest request);

    RelicVO updateRelic(String objectId, UpdateRelicRequest request);

    DeleteRelicVO deleteRelic(String objectId);

    RelicImageUploadVO uploadRelicImage(String objectId, MultipartFile file);

    RelicCsvImportResultVO importRelicsFromCsv(MultipartFile file);
}
