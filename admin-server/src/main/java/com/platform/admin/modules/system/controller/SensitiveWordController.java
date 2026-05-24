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
@RequestMapping("/api/v1/config/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SensitiveWord> pageRequest = new Page<>(page, pageSize);
        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("word", keyword);
        }
        wrapper.orderByDesc("create_time");
        IPage<SensitiveWord> result = sensitiveWordService.page(pageRequest, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords().stream().map(word -> {
            Map<String, Object> wordMap = new HashMap<>();
            wordMap.put("objectId", word.getObjectId());
            wordMap.put("word", word.getWord());
            wordMap.put("createTime", word.getCreateTime());
            return wordMap;
        }).toList());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        
        return Result.success(data);
    }

    @GetMapping("/{objectId}")
    public Result<Map<String, Object>> getById(@PathVariable String objectId) {
        SensitiveWord word = sensitiveWordService.getById(objectId);
        if (word == null) {
            return Result.error(ErrorCode.NOT_FOUND, "敏感词不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", word.getObjectId());
        data.put("word", word.getWord());
        data.put("createTime", word.getCreateTime());
        
        return Result.success(data);
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWordService.save(sensitiveWord);
        
        Map<String, Object> data = new HashMap<>();
        data.put("objectId", sensitiveWord.getObjectId());
        data.put("word", sensitiveWord.getWord());
        data.put("createTime", sensitiveWord.getCreateTime());
        
        return Result.success(data);
    }

    @DeleteMapping("/{objectId}")
    public Result<Void> delete(@PathVariable String objectId) {
        if (!sensitiveWordService.removeById(objectId)) {
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