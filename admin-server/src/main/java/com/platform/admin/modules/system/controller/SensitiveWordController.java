package com.platform.admin.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.modules.system.entity.SensitiveWord;
import com.platform.admin.modules.system.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping
    public Result<IPage<SensitiveWord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<SensitiveWord> pageRequest = new Page<>(page, size);
        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("word", keyword);
        }
        wrapper.orderByDesc("create_time");
        IPage<SensitiveWord> result = sensitiveWordService.page(pageRequest, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SensitiveWord> getById(@PathVariable String id) {
        SensitiveWord word = sensitiveWordService.getById(id);
        if (word == null) {
            return Result.error(ErrorCode.NOT_FOUND, "敏感词不存在");
        }
        return Result.success(word);
    }

    @PostMapping
    public Result<SensitiveWord> create(@RequestBody SensitiveWord word) {
        sensitiveWordService.save(word);
        return Result.success(word);
    }

    @PutMapping("/{id}")
    public Result<SensitiveWord> update(@PathVariable String id, @RequestBody SensitiveWord word) {
        SensitiveWord existing = sensitiveWordService.getById(id);
        if (existing == null) {
            return Result.error(ErrorCode.NOT_FOUND, "敏感词不存在");
        }
        word.setObjectId(id);
        sensitiveWordService.updateById(word);
        return Result.success(word);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        if (!sensitiveWordService.removeById(id)) {
            return Result.error(ErrorCode.NOT_FOUND, "敏感词不存在");
        }
        return Result.success(null);
    }

    @PostMapping("/check")
    public Result<Map<String, Object>> checkContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        boolean hasSensitive = sensitiveWordService.checkContent(content);
        Map<String, Object> result = new HashMap<>();
        result.put("hasSensitive", hasSensitive);
        return Result.success(result);
    }

    @PostMapping("/filter")
    public Result<Map<String, Object>> filterContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String filtered = sensitiveWordService.filterContent(content);
        Map<String, Object> result = new HashMap<>();
        result.put("original", content);
        result.put("filtered", filtered);
        return Result.success(result);
    }
}