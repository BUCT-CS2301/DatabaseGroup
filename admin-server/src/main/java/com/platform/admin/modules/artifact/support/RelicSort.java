package com.platform.admin.modules.artifact.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import org.springframework.util.StringUtils;

/**
 * 文物浏览列表排序方式（M2 PRD）。
 */
public enum RelicSort {
    HOT("hot"),
    NAME("name"),
    PERIOD("period");

    private final String queryValue;

    RelicSort(String queryValue) {
        this.queryValue = queryValue;
    }

    public String queryValue() {
        return queryValue;
    }

    /**
     * 解析 sort 参数；空或未传时默认热度排序。
     *
     * @param sort Query 参数 sort
     * @return 排序枚举
     * @throws BusinessException sort 非空且不在枚举内时 400
     */
    public static RelicSort parse(String sort) {
        if (!StringUtils.hasText(sort)) {
            return HOT;
        }
        String normalized = sort.strip().toLowerCase();
        for (RelicSort value : values()) {
            if (value.queryValue.equals(normalized)) {
                return value;
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "sort 取值须为 hot、name 或 period");
    }
}
