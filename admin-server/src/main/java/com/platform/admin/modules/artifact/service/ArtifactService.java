package com.platform.admin.modules.artifact.service;

import com.platform.admin.common.PageResult;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import org.springframework.web.multipart.MultipartFile;
import com.platform.admin.modules.artifact.vo.ArtifactSearchResultVO;


public interface ArtifactService {
    
    PageResult<RelicVO> pageRelics(long page, long pageSize, String keyword, String museumId);

    RelicVO getRelicById(String objectId);

    ArtifactSearchResultVO searchArtifacts(String q, long page, long size);

    RelicVO createRelic(CreateRelicRequest request);

    RelicVO updateRelic(String objectId, UpdateRelicRequest request);

    DeleteRelicVO deleteRelic(String objectId);

    RelicImageUploadVO uploadRelicImage(String objectId, MultipartFile file);

    RelicCsvImportResultVO importRelicsFromCsv(MultipartFile file);
}
