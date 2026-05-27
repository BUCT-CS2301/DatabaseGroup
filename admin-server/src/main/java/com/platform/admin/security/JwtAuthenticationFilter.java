package com.platform.admin.security;

import com.platform.admin.modules.log.support.LogPermissionResolver;
import com.platform.admin.modules.user.entity.User;
import com.platform.admin.modules.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
                                   UserMapper userMapper,
                                   LogPermissionResolver logPermissionResolver) {
        this.jwtProvider = jwtProvider;
        this.userMapper = userMapper;
        this.logPermissionResolver = logPermissionResolver;
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
                User user = userMapper.selectById(userId);
                if (user != null && !"DISABLED".equals(user.getStatus())) {
                    UserType userType = UserType.fromValue(user.getUserType());
                    List<String> logPermissions = logPermissionResolver.resolve(user);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userType.name()));
                    logPermissions.forEach(permission ->
                            authorities.add(new SimpleGrantedAuthority(permission))
                    );
                    AuthUser authUser = new AuthUser(userId, userType, Set.copyOf(logPermissions));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            authUser, null, authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
