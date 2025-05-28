package com.example.demo.api.common.service;

import com.example.demo.entity.jpa.user.UserEntity;
import com.example.demo.entity.jpa.user.UserActivity;
import com.example.demo.enums.UserRole;
import com.example.demo.repo.jpa.user.UserRepository;
import com.example.demo.repo.jpa.user.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 🚨 Log4j 취약점: 로그인 시도 로깅
        log.info("Login attempt for username: {}", username);

        UserEntity user = userRepository.findByUsernameOrEmail(username)
        .orElseThrow(() -> {
            log.warn("User not found during login: {}", username);
            return new UsernameNotFoundException("User not found: " + username);
        });

        log.info("User found for login: {}, enabled: {}", user.getUsername(), user.isEnabled());
        return user;
    }

    @Transactional
    public UserEntity registerUser(String username, String email, String password, String nickname, 
                           HttpServletRequest request) {
        // 중복 체크
        if (userRepository.existsByUsername(username)) {
            // 🚨 Log4j 취약점: 사용자 입력을 직접 로깅 (JNDI Injection 가능)
            log.warn("Registration failed - Username already exists: {}", username);
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            // 🚨 Log4j 취약점: 이메일 입력을 직접 로깅
            log.warn("Registration failed - Email already exists: {}", email);
            throw new RuntimeException("Email already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(UserRole.USER)
                .build();

        UserEntity savedUser = userRepository.save(user);
        
        // 🚨 Log4j 취약점: User-Agent 헤더를 직접 로깅
        String userAgent = request.getHeader("User-Agent");
        log.info("New user registered: {} from IP: {} with User-Agent: {}", 
                username, getClientIpAddress(request), userAgent);
        
        // 활동 로그 저장
        UserActivity activity = UserActivity.builder()
                .username(username)
                .activity("USER_REGISTRATION")
                .ipAddress(getClientIpAddress(request))
                .userAgent(userAgent)
                .success(true)
                .build();
        userActivityRepository.save(activity);

        return savedUser;
    }

    @Transactional
    public boolean authenticateUser(String username, String password, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        
        // 🚨 Log4j 취약점: 로그인 시도 정보를 직접 로깅
        log.info("Login attempt for user: {} from IP: {} with User-Agent: {}", 
                username, ipAddress, userAgent);

        Optional<UserEntity> userOpt = userRepository.findByUsernameOrEmail(username);
        
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            // 성공 로그
            log.info("Successful login for user: {} from IP: {}", username, ipAddress);
            
            // 성공 활동 기록
            UserActivity successActivity = UserActivity.loginAttempt(username, ipAddress, userAgent, true);
            userActivityRepository.save(successActivity);
            
            return true;
        } else {
            // 🚨 Log4j 취약점: 실패한 로그인 정보를 상세히 로깅
            log.warn("Failed login attempt for user: {} from IP: {} with User-Agent: {} - Invalid credentials", 
                    username, ipAddress, userAgent);
            
            // 실패 활동 기록
            UserActivity failActivity = UserActivity.loginAttempt(username, ipAddress, userAgent, false);
            userActivityRepository.save(failActivity);
            
            return false;
        }
    }

    public Page<UserEntity> searchUsers(String keyword, Pageable pageable, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 검색 키워드를 직접 로깅 (JNDI Injection의 주요 벡터)
        log.info("User search performed with keyword: {} from IP: {}", 
                keyword, getClientIpAddress(request));
        
        // 검색 활동 기록
        UserActivity searchActivity = UserActivity.searchActivity(
                getCurrentUsername(request), 
                getClientIpAddress(request), 
                request.getHeader("User-Agent"), 
                keyword
        );
        userActivityRepository.save(searchActivity);
        
        return userRepository.findByKeyword(keyword, pageable);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public Page<UserEntity> findAllUsers(Pageable pageable) {
        return userRepository.findByEnabledTrue(pageable);
    }

    @Transactional
    public void updateUserProfile(Long userId, String email, String nickname, HttpServletRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 🚨 Log4j 취약점: 프로필 업데이트 정보를 로깅
        log.info("Profile update for user: {} - Email: {}, Nickname: {} from IP: {}", 
                user.getUsername(), email, nickname, getClientIpAddress(request));
        
        user.updateProfile(email, nickname);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long userId, HttpServletRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 🚨 Log4j 취약점: 사용자 비활성화 로그
        log.warn("User disabled: {} by admin from IP: {}", 
                user.getUsername(), getClientIpAddress(request));
        
        user.disable();
        userRepository.save(user);
    }

    // 활동 로그 조회
    public Page<UserActivity> getUserActivities(String username, Pageable pageable) {
        return userActivityRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
    }

    public Page<UserActivity> getFailedLoginAttempts(Pageable pageable) {
        return userActivityRepository.findFailedLoginAttempts(pageable);
    }

    // 🔍 보안 모니터링: JNDI 공격 탐지
    public void detectPotentialAttacks() {
        var potentialAttacks = userActivityRepository.findPotentialJndiAttacks();
        if (!potentialAttacks.isEmpty()) {
            log.error("SECURITY ALERT: Potential JNDI injection attacks detected! Count: {}", 
                    potentialAttacks.size());
            
            potentialAttacks.forEach(activity -> 
                log.error("Suspicious activity - User: {}, IP: {}, Details: {}, UserAgent: {}", 
                        activity.getUsername(), activity.getIpAddress(), 
                        activity.getDetails(), activity.getUserAgent())
            );
        }
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

    private String getCurrentUsername(HttpServletRequest request) {
        // 실제 구현에서는 Security Context에서 가져옴
        return request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous";
    }
}