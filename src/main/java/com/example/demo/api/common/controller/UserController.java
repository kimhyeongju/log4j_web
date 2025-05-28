package com.example.demo.api.common.controller;

import com.example.demo.api.auth.dto.LoginRequestDto;
import com.example.demo.api.common.dto.UserRegistrationDto;
import com.example.demo.api.common.dto.UserSearchDto;
import com.example.demo.api.common.service.UserService;
import com.example.demo.entity.jpa.user.UserActivity;
import com.example.demo.entity.jpa.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final UserService userService;

    /**
     * 사용자 회원가입
     * 🚨 Log4j 취약점: 회원가입 시 User-Agent 헤더 로깅
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto dto, 
                                         HttpServletRequest request) {
        try {
            UserEntity user = userService.registerUser(
                    dto.getUsername(), 
                    dto.getEmail(), 
                    dto.getPassword(), 
                    dto.getNickname(), 
                    request
            );
            
            return ResponseEntity.ok().body("User registered successfully: " + user.getUsername());
        } catch (Exception e) {
            // 🚨 Log4j 취약점: 에러 메시지에 사용자 입력 포함
            log.error("Registration failed for username: {}, error: {}", 
                    dto.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
        try {
            // 🚨 Log4j 취약점: 로그인 요청 로깅
            log.info("Login API called - Username: {}, IP: {}, User-Agent: {}", 
                    loginRequest.getUsername(), 
                    getClientIpAddress(request), 
                    request.getHeader("User-Agent"));
            
            boolean success = userService.authenticateUser(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword(), 
                    request
            );
            
            if (success) {
                return ResponseEntity.ok().body("Login successful");
            } else {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
        } catch (Exception e) {
            log.error("Login error for username: {}, error: {}", 
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    /**
     * 사용자 검색 (Log4j 취약점의 주요 공격 벡터)
     * 🚨 검색어에 JNDI 문자열 삽입 가능
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserEntity>> searchUsers(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 검색 파라미터를 직접 로깅
        log.info("User search request - keyword: {}, page: {}, size: {}, IP: {}", 
                keyword, page, size, getClientIpAddress(request));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> users = userService.searchUsers(keyword, pageable, request);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 사용자 조회 로그
        log.info("User detail request - ID: {}, IP: {}, User-Agent: {}", 
                id, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<UserEntity>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> users = userService.findAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 프로필 업데이트
     * 🚨 Log4j 취약점: 업데이트 정보 로깅
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                          @RequestParam String email,
                                          @RequestParam String nickname,
                                          HttpServletRequest request) {
        try {
            // 🚨 Log4j 취약점: 업데이트 파라미터를 직접 로깅
            log.info("Profile update request - UserID: {}, Email: {}, Nickname: {}, IP: {}", 
                    id, email, nickname, getClientIpAddress(request));
            
            userService.updateUserProfile(id, email, nickname, request);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            log.error("Profile update failed for user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    /**
     * 사용자 활동 로그 조회
     */
    @GetMapping("/{username}/activities")
    public ResponseEntity<Page<UserActivity>> getUserActivities(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activities = userService.getUserActivities(username, pageable);
        
        return ResponseEntity.ok(activities);
    }

    /**
     * 로그인 실패 기록 조회 (관리자용)
     */
    @GetMapping("/failed-logins")
    public ResponseEntity<Page<UserActivity>> getFailedLoginAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> failedLogins = userService.getFailedLoginAttempts(pageable);
        
        return ResponseEntity.ok(failedLogins);
    }

    /**
     * 보안 모니터링: JNDI 공격 탐지 (관리자용)
     */
    @PostMapping("/security/detect-attacks")
    public ResponseEntity<?> detectPotentialAttacks(HttpServletRequest request) {
        // 🚨 Log4j 취약점: 보안 검사 요청 로깅
        log.info("Security check requested from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        userService.detectPotentialAttacks();
        return ResponseEntity.ok("Security check completed. Check logs for details.");
    }

    /**
     * 사용자 비활성화 (관리자용)
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            userService.disableUser(id, request);
            return ResponseEntity.ok("User disabled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Disable failed: " + e.getMessage());
        }
    }

    /**
     * 🚨 특별한 Log4j 취약점 실습 엔드포인트
     * 사용자명 검증 (의도적으로 취약하게 작성)
     */
    @GetMapping("/validate-username")
    public ResponseEntity<?> validateUsername(@RequestParam String username, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 사용자명 검증 시 입력값 직접 로깅
        log.info("Username validation request: {}", username);
        log.debug("Validating username: '{}' from IP: {}", username, getClientIpAddress(request));
        
        if (userService.findByUsername(username).isPresent()) {
            log.warn("Username already exists: {}", username);
            return ResponseEntity.badRequest().body("Username already exists: " + username);
        }
        
        log.info("Username available: {}", username);
        return ResponseEntity.ok("Username is available: " + username);
    }

    /**
     * 🚨 디버그용 헤더 정보 로깅 엔드포인트
     */
    @GetMapping("/debug/headers")
    public ResponseEntity<?> debugHeaders(HttpServletRequest request) {
        // 🚨 Log4j 취약점: 모든 HTTP 헤더를 로깅
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.debug("Header - {}: {}", headerName, headerValue);
        });
        
        return ResponseEntity.ok("Headers logged for debugging");
    }

    // 유틸리티 메서드
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
