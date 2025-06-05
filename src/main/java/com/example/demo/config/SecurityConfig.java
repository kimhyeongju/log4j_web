package com.example.demo.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Log4j2
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 🚨 Log4j 취약점: 보안 설정 로깅
        log.info("Configuring security settings for Log4j vulnerability demo");
        
        http
            // CSRF 비활성화 (실습 편의를 위해)
            .csrf().disable()
            
            // CORS 설정
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // 세션 관리
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            
            .and()
            
            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 퍼블릭 엔드포인트 (로그인 없이 접근 가능)
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()              // 로그인 페이지
                .antMatchers("/register").permitAll()           // 회원가입 페이지
                .antMatchers("/api/vulnerable/**").permitAll()  // 🚨 취약점 실습용 엔드포인트
                .antMatchers("/api/users/register").permitAll() // 회원가입
                .antMatchers("/api/users/search").permitAll()   // 🚨 검색 기능 (취약점 벡터)
                .antMatchers("/api/users/validate-username").permitAll()  // 🚨 사용자명 검증
                .antMatchers("/api/users/debug/**").permitAll() // 🚨 디버그 엔드포인트
                .antMatchers("/api/boards/search/**").permitAll() // 🚨 게시판 검색 (주요 공격 벡터)
                .antMatchers("/api/boards/validate-title").permitAll() // 🚨 제목 검증
                .antMatchers("/api/boards/debug/**").permitAll() // 🚨 디버그 엔드포인트
                .antMatchers("/api/boards").permitAll()         // 게시판 목록 조회
                .antMatchers("/api/boards/{id}").permitAll()    // 게시글 상세 조회
                .antMatchers("/warehouse").permitAll()          // 입고 관리 (실습용)
                .antMatchers("/inventory").permitAll()          // 재고 조회 (실습용)
                .antMatchers("/vulnerable").permitAll()         // 취약점 실습 페이지
                
                // 정적 리소스
                .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // 관리 엔드포인트
                .antMatchers("/actuator/**").permitAll()
                
                // H2 Console (테스트용)
                .antMatchers("/h2-console/**").permitAll()
                
                // 관리자 페이지는 인증 필요
                .antMatchers("/admin/**").hasRole("ADMIN")
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // 커스텀 로그인 페이지 설정
            .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/perform_login")
            .defaultSuccessUrl("/", true)
            .failureUrl("/login?error=true")
            .usernameParameter("username")
            .passwordParameter("password")
            .permitAll()
        )
            
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // 헤더 설정
            .headers(headers -> headers
                .frameOptions().disable()  // H2 Console 사용을 위해
                .contentTypeOptions().disable()
            );

        // 🚨 Log4j 취약점: 보안 필터 체인 구성 완료 로깅
        log.info("Security filter chain configured with custom login page for vulnerability demo");
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 🚨 Log4j 취약점: CORS 설정 로깅
        log.info("Configuring CORS settings");
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 수정: source -> configuration
        
        return source;
    }
}