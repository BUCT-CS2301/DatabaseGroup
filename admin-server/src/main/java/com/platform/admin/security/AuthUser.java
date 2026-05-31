package com.platform.admin.security;

import java.util.Set;

public record AuthUser(String objectId, UserType userType, Set<String> permissions) {
}
