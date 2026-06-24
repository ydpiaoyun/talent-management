package com.talent.config;

import com.talent.common.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Spring Security 安全配置
 * <p>
 * 基于 JWT 的无状态认证，配置 CORS 跨域、路由权限、JWT 过滤器。
 * </p>
 *
 * @author talent-hr
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtUtil jwtUtil;

    private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();

    /**
     * 构造方法
     *
     * @param jwtUtil JWT 工具类
     */
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 配置安全过滤链
     *
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**", "/h2-console/**", "/doc.html", "/v3/api-docs/**",
                        "/swagger-ui/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(fo -> fo.disable()))
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 密码编码器（BCrypt）
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 跨域配置
     *
     * @return CorsConfigurationSource 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * JWT 认证过滤器
     * <p>
     * 从请求头提取 Token 并校验，有效则设置认证上下文。
     * </p>
     *
     * @return OncePerRequestFilter 实例
     */
    @Bean
    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                String path = request.getRequestURI();

                // 放行 CORS 预检请求
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    chain.doFilter(request, response);
                    return;
                }

                // 放行认证接口和静态资源
                if (path.startsWith("/api/auth/") || path.startsWith("/h2-console")
                        || path.startsWith("/doc.html") || path.startsWith("/v3/api-docs")
                        || path.startsWith("/swagger-ui")) {
                    chain.doFilter(request, response);
                    return;
                }

                // 提取并校验 JWT Token
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer")) {
                    String token = header.substring(7).trim();
                    log.debug("JWT 过滤器: 路径={}, Token={}...", path,
                            token.substring(0, Math.min(10, token.length())));

                    if (jwtUtil.validate(token)) {
                        String username = jwtUtil.getUsername(token);
                        log.info("JWT 认证成功: 用户={}", username);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        username, null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                        // 保存到 SecurityContextHolder 和 SecurityContextRepository
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(auth);
                        SecurityContextHolder.setContext(context);
                        securityContextRepository.saveContext(context, request, response);

                        log.debug("JWT 过滤器: 认证状态={}", auth.isAuthenticated());
                        chain.doFilter(request, response);
                        return;
                    }
                    log.debug("JWT 过滤器: Token 无效");
                }

                // 未认证或 Token 过期
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            }
        };
    }
}
