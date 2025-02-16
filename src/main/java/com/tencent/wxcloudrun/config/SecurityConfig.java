package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.security.WxAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 微信登录相关接口
                .antMatchers("/api/auth/wx-login").permitAll()
                .antMatchers("/api/auth/check-token").permitAll()
                // 其他公开接口
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/error").permitAll()
                // Swagger相关路径（如果需要）
                .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs").permitAll()
                // 需要认证的接口
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(wxAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public WxAuthenticationFilter wxAuthenticationFilter() {
        return new WxAuthenticationFilter();
    }
} 