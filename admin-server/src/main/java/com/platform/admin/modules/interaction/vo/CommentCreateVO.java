package com.platform.admin.modules.interaction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentCreateVO {
    private String objectId;
    private String status;
    private String message;
}
