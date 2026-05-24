package com.platform.admin.modules.museum.controller;

import com.platform.admin.common.PageResult;
import com.platform.admin.common.Result;
import com.platform.admin.modules.museum.dto.CreateMuseumRequest;
import com.platform.admin.modules.museum.dto.UpdateMuseumRequest;
import com.platform.admin.modules.museum.service.MuseumService;
import com.platform.admin.modules.museum.vo.DeleteMuseumVO;
import com.platform.admin.modules.museum.vo.MuseumVO;
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
@RequestMapping("/api/v1/data/museums")
public class MuseumController {

    private final MuseumService museumService;

    public MuseumController(MuseumService museumService) {
        this.museumService = museumService;
    }

    @GetMapping
    public Result<PageResult<MuseumVO>> pageMuseums(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page最小为1") Long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize最小为1") Long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(museumService.pageMuseums(page, pageSize, keyword));
    }

    @GetMapping("/{objectId}")
    public Result<MuseumVO> getMuseum(@PathVariable String objectId) {
        return Result.success(museumService.getMuseumById(objectId));
    }

    @PostMapping
    public Result<MuseumVO> createMuseum(@RequestBody @Valid CreateMuseumRequest request) {
        return Result.success(museumService.createMuseum(request));
    }

    @PutMapping("/{objectId}")
    public Result<MuseumVO> updateMuseum(
            @PathVariable String objectId,
            @RequestBody @Valid UpdateMuseumRequest request
    ) {
        return Result.success(museumService.updateMuseum(objectId, request));
    }

    @DeleteMapping("/{objectId}")
    public Result<DeleteMuseumVO> deleteMuseum(@PathVariable String objectId) {
        return Result.success(museumService.deleteMuseum(objectId));
    }
}
