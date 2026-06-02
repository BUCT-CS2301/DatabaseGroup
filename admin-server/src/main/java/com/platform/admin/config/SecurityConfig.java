package com.platform.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.common.Result;
import com.platform.admin.common.log.SecurityLogWriter;
import com.platform.admin.modules.log.support.LogPermissions;
import com.platform.admin.security.AuthUser;
import com.platform.admin.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final SecurityLogWriter securityLogWriter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper,
                          SecurityLogWriter securityLogWriter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
        this.securityLogWriter = securityLogWriter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh-token", "/api/v1/auth/register", "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/mock-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/logs/export").hasAuthority(LogPermissions.EXPORT)
                .requestMatchers(HttpMethod.GET, "/api/v1/logs/download").hasAuthority(LogPermissions.EXPORT)
                .requestMatchers(HttpMethod.GET, "/api/v1/logs/operation", "/api/v1/logs/operation/**",
                        "/api/v1/logs/system", "/api/v1/logs/security").hasAuthority(LogPermissions.READ)
                .requestMatchers("/api/v1/logs/**").authenticated()
                .requestMatchers("/api/v1/data/relics/**", "/api/v1/data/museums/**").authenticated()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    recordAccessDenied(request, "UNAUTHORIZED");
                    response.setStatus(401);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Result.error(ErrorCode.UNAUTHORIZED, "未认证或Token失效")
                    ));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    recordAccessDenied(request, "FORBIDDEN");
                    response.setStatus(403);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Result.error(ErrorCode.FORBIDDEN, "无操作权限")
                    ));
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void recordAccessDenied(HttpServletRequest request, String denyReason) {
        securityLogWriter.writeAccessDenied(
                resolveUserId(request),
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getMethod(),
                denyReason
        );
    }

    private String resolveUserId(HttpServletRequest request) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
            return authUser.objectId();
        }
        return null;
    }
}
