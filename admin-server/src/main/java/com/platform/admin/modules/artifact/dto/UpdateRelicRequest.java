package com.platform.admin.modules.artifact.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateRelicRequest {
    @Size(max = 500, message = "title长度不能超过500")
    private String title;

    @Size(max = 200, message = "period长度不能超过200")
    private String period;

    @Size(max = 100, message = "type长度不能超过100")
    private String type;

    @Size(max = 200, message = "material长度不能超过200")
    private String material;

    private String description;

    @Size(max = 300, message = "dimensions长度不能超过300")
    private String dimensions;

    private String museumId;

    @Size(max = 1000, message = "detailUrl长度不能超过1000")
    private String detailUrl;

    @Size(max = 500, message = "creditLine长度不能超过500")
    private String creditLine;

    @Size(max = 100, message = "accessionNumber长度不能超过100")
    private String accessionNumber;

    private LocalDate crawlDate;
}
