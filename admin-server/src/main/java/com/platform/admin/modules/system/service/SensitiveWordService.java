package com.platform.admin.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.admin.modules.system.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {
    List<SensitiveWord> getAllWords();
    boolean checkContent(String content);
    String filterContent(String content);
}