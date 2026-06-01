package com.platform.admin.modules.interaction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemsPageVO<T> {
    private long total;
    private long page;
    private long size;
    private List<T> items;
}
