package com.platform.admin.modules.artifact.controller;

import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import com.platform.admin.modules.artifact.dto.UpdateRelicRequest;
import com.platform.admin.modules.artifact.service.ArtifactService;
import com.platform.admin.modules.artifact.vo.DeleteRelicVO;
import com.platform.admin.modules.artifact.vo.RelicVO;
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
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/data/relics")
public class RelicController {

    private final ArtifactService artifactService;

    public RelicController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @GetMapping
    public Result<PageResult<RelicVO>> pageRelics(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize最小为1") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String museumId
    ) {
        return Result.success(artifactService.pageRelics(page, pageSize, keyword, museumId));
    }

    @GetMapping("/{objectId}")
    public Result<RelicVO> getRelic(@PathVariable String objectId) {
        return Result.success(artifactService.getRelicById(objectId));
    }

    @PostMapping
    public Result<RelicVO> createRelic(@RequestBody @Valid CreateRelicRequest request) {
        return Result.success(artifactService.createRelic(request));
    }

    @PutMapping("/{objectId}")
    public Result<RelicVO> updateRelic(@PathVariable String objectId, @RequestBody @Valid UpdateRelicRequest request) {
        return Result.success(artifactService.updateRelic(objectId, request));
    }

    @DeleteMapping("/{objectId}")
    public Result<DeleteRelicVO> deleteRelic(@PathVariable String objectId) {
        return Result.success(artifactService.deleteRelic(objectId));
    }
}
