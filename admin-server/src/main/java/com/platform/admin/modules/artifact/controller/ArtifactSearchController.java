package com.platform.admin.modules.artifact.controller;

import com.platform.admin.common.Result;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.vo.ArtifactSearchResultVO;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/artifacts")
public class ArtifactSearchController {

    private final ArtifactService artifactService;

    public ArtifactSearchController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @GetMapping("/search")
    public Result<ArtifactSearchResultVO> searchArtifacts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size最小为1") Long size
    ) {
        return Result.success(artifactService.searchArtifacts(q, page, size));
    }
}