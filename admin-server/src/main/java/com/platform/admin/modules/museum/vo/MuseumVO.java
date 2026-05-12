package com.platform.admin.modules.museum.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MuseumVO {
    private String objectId;
    private String name;
    private String nameCn;
    private String location;
    private String website;
}
