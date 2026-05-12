package com.platform.admin.modules.museum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMuseumRequest {
    @NotBlank(message = "name不能为空")
    @Size(max = 200, message = "name长度不能超过200")
    private String name;

    @Size(max = 200, message = "nameCn长度不能超过200")
    private String nameCn;

    @Size(max = 200, message = "location长度不能超过200")
    private String location;

    @Size(max = 500, message = "website长度不能超过500")
    private String website;
}
