package com.platform.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.admin.modules.system.entity.SensitiveWord;
import com.platform.admin.modules.system.mapper.SensitiveWordMapper;
import com.platform.admin.modules.system.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {

    @Override
    public List<SensitiveWord> getAllWords() {
        return list();
    }

    @Override
    public boolean checkContent(String content) {
        List<SensitiveWord> words = getAllWords();
        for (SensitiveWord word : words) {
            if (content.contains(word.getWord())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String filterContent(String content) {
        List<SensitiveWord> words = getAllWords();
        String result = content;
        for (SensitiveWord word : words) {
            String replacement = "*".repeat(word.getWord().length());
            Pattern pattern = Pattern.compile(Pattern.quote(word.getWord()));
            Matcher matcher = pattern.matcher(result);
            result = matcher.replaceAll(replacement);
        }
        return result;
    }

    @Override
    public boolean save(SensitiveWord entity) {
        entity.setObjectId(UUID.randomUUID().toString());
        entity.setCreateTime(LocalDateTime.now());
        return super.save(entity);
    }
}