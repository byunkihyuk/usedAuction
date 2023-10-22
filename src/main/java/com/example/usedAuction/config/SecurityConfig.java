package com.example.usedAuction.config;

import com.example.usedAuction.config.jwt.JwtAccessDeniedHandler;
import com.example.usedAuction.config.jwt.JwtAuthenticationEntryPoint;
import com.example.usedAuction.config.jwt.JwtSecurityConfig;
import com.example.usedAuction.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration // 해당 클래스를 Configuration으로 등록
@EnableWebSecurity // spring security를 활성화
@EnableMethodSecurity // @PreAuthorize("hasRole('ROLE_ADMIN')") 권한 별 접근 통제
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http)throws  Exception{
        http
                .csrf().disable()
                .addFilterAfter(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic().disable()
                .formLogin().disable()
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //.requestMatchers("api/general/**").authenticated()
                        .anyRequest().permitAll() // 나머지 요청 모두 허용
                )
                .apply(new JwtSecurityConfig(tokenProvider))
                .and()
                .exceptionHandling()
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .authenticationEntryPoint(jwtAuthenticationEntryPoint);
        return http.build();
    }

}
