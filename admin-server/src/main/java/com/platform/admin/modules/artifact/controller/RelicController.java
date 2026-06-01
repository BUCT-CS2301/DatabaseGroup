package com.platform.admin.modules.artifact.controller;

import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicCsvImportResultVO;
import com.platform.admin.modules.artifact.vo.RelicFiltersVO;
import com.platform.admin.modules.artifact.vo.RelicImageUploadVO;
import com.platform.admin.modules.artifact.vo.RelicRelatedVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1/data/relics")
public class RelicController {

    private static final Logger log = LoggerFactory.getLogger(RelicController.class);

    private final ArtifactService artifactService;

    public RelicController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @GetMapping
    public Result<PageResult<RelicVO>> pageRelics(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize最小为1") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String museumId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String sort
    ) {
        return Result.success(
                artifactService.pageRelics(page, pageSize, keyword, museumId, period, type, material, sort));
    }

    /**
     * 文物浏览筛选项（须在 {@code /{objectId}} 之前注册，避免路径冲突）。
     */
    @GetMapping("/filters")
    public Result<RelicFiltersVO> getRelicFilters() {
        return Result.success(artifactService.getRelicFilters());
    }

    @GetMapping("/{objectId}/related")
    public Result<RelicRelatedVO> getRelicRelated(@PathVariable String objectId) {
        return Result.success(artifactService.getRelicRelated(objectId));
    }

    @GetMapping("/{objectId}")
    public Result<RelicVO> getRelic(@PathVariable String objectId) {
        return Result.success(artifactService.getRelicById(objectId));
    }

    @PostMapping
    public Result<RelicVO> createRelic(@RequestBody @Valid CreateRelicRequest request) {
        return Result.success(artifactService.createRelic(request));
    }

    /**
     * 管理员 CSV 批量创建文物（multipart 字段 {@code file}）
     */
    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RelicCsvImportResultVO> importRelicsFromCsv(@RequestPart("file") MultipartFile file) {
        return Result.success(artifactService.importRelicsFromCsv(file));
    }

    @PutMapping("/{objectId}")
    public Result<RelicVO> updateRelic(@PathVariable String objectId, @RequestBody @Valid UpdateRelicRequest request) {
        return Result.success(artifactService.updateRelic(objectId, request));
    }

    @DeleteMapping("/{objectId}")
    public Result<DeleteRelicVO> deleteRelic(@PathVariable String objectId) {
        return Result.success(artifactService.deleteRelic(objectId));
    }

    /**
     * 管理员上传文物图片（multipart 字段 {@code file}）
     */
    @PostMapping(value = "/{objectId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RelicImageUploadVO> uploadRelicImage(
            @PathVariable String objectId,
            @RequestPart("file") MultipartFile file) {
        log.info("event=relic_image_upload_start object_id={}", objectId);
        return Result.success(artifactService.uploadRelicImage(objectId, file));
    }
}
