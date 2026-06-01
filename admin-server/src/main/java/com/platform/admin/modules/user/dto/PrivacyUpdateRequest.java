package com.platform.admin.modules.user.dto;

import lombok.Data;

@Data
public class PrivacyUpdateRequest {
    private Boolean favoritesVisible;
    private Boolean likesVisible;
    private Boolean commentsVisible;
    private Boolean uploadsVisible;
}