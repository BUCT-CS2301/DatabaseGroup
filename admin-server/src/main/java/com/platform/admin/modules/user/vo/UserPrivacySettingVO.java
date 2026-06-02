package com.platform.admin.modules.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrivacySettingVO {
    private Boolean favoritesVisible;
    private Boolean likesVisible;
    private Boolean commentsVisible;
    private Boolean uploadsVisible;
}