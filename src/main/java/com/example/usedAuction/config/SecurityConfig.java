package com.example.usedAuction.config;

import com.example.usedAuction.config.jwt.JwtAccessDeniedHandler;
import com.example.usedAuction.config.jwt.JwtAuthenticationEntryPoint;
import com.example.usedAuction.config.jwt.JwtSecurityConfig;
import com.example.usedAuction.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

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
                .exceptionHandling()
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                //.httpBasic().disable()
                //.formLogin().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/api/sign-up").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/sign-up/username-check").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/sign-up/nickname-check").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/sign-in").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/general/*").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/general").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/auction/*").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/auction").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/main").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/user/*").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/user/*/general-sell-history").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/user/*/auction-sell-history").permitAll()
                        .antMatchers(HttpMethod.GET, "/chatting").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .apply(new JwtSecurityConfig(tokenProvider));
        return http.build();
    }

}

