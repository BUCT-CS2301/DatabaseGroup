package com.platform.admin.common;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long pageSize;

    public PageResult(List<T> records, long total, long page, long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() { return records; }
    public long getTotal() { return total; }
    public long getPage() { return page; }
    public long getPageSize() { return pageSize; }
}
