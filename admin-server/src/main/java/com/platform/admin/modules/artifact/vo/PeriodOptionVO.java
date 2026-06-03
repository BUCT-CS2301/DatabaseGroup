package com.platform.admin.modules.artifact.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodOptionVO {

    private String value;

    private String label;

    private Integer order;
}