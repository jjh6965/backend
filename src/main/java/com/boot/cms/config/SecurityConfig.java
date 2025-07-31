package com.boot.cms.config;

import com.boot.cms.entity.auth.LoginEntity;
import com.boot.cms.mapper.auth.LoginMapper;
import com.boot.cms.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    private final Environment environment;
    private final LoginMapper loginMapper;

    public SecurityConfig(Environment environment, LoginMapper loginMapper) {
        this.environment = environment;
        this.loginMapper = loginMapper;
    }

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isRender = isRenderEnvironment();
        if (isRender) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/cms/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/public/**", "/api/auth/**", "/api/naver/**", "/cms/api/naver/**", "/api/chatbot/**").permitAll() // 하위 경로 포함하도록 수정
                        .requestMatchers("/api/**", "/cms/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"success\": false, \"message\": \"Unauthorized\"}");
                        })
                );

        return http.build();
    }

    private boolean isRenderEnvironment() {
        String env = environment.getProperty("PORT");
        return env != null && !env.isEmpty();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = getCorsConfiguration();
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/cms/api/**", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        return source;
    }

    @NotNull
    private CorsConfiguration getCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();

        String[] originsArray = allowedOrigins.split(",");
        System.out.println("allowedOrigins: " + String.join(", ", originsArray));

        for (String origin : originsArray) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isEmpty()) {
                configuration.addAllowedOrigin(trimmedOrigin);
            }
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        return configuration;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            LoginEntity user = loginMapper.loginCheck(username, null);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return new User(
                    user.getEmpNo(),
                    user.getEmpPwd(),
                    Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getAuth()))
            );
        };
    }
}