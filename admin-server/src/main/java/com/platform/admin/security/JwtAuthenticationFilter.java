package com.platform.admin.security;

import com.platform.admin.modules.log.support.LogPermissionResolver;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final LogPermissionResolver logPermissionResolver;
    private final boolean trustMockTokenClaims;

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
                                   UserMapper userMapper,
                                   LogPermissionResolver logPermissionResolver,
                                   @Value("${spring.profiles.active:}") String activeProfile) {
        this.jwtProvider = jwtProvider;
        this.userMapper = userMapper;
        this.logPermissionResolver = logPermissionResolver;
        this.trustMockTokenClaims = activeProfile.contains("dev");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            if (jwtProvider.validateToken(token)) {
                Claims claims = jwtProvider.getClaims(token);
                String userId = claims.getSubject();
                String userTypeClaim = claims.get("userType", String.class);
                User user = userMapper.selectById(userId);
                if (user != null && !"DISABLED".equals(user.getStatus())) {
                    establishAuthentication(userId, UserType.fromValue(user.getUserType()), logPermissionResolver.resolve(user), request);
                } else if (trustMockTokenClaims) {
                    // dev 环境 mock token 允许按 claim 放权，便于联调
                    UserType userType = UserType.fromValue(userTypeClaim);
                    User mockUser = new User();
                    mockUser.setUserType(userType.name());
                    mockUser.setStatus("ACTIVE");
                    establishAuthentication(userId, userType, logPermissionResolver.resolve(mockUser), request);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void establishAuthentication(
            String userId,
            UserType userType,
            List<String> logPermissions,
            HttpServletRequest request
    ) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userType.name()));
        logPermissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        AuthUser authUser = new AuthUser(userId, userType, Set.copyOf(logPermissions));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authUser, null, authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
