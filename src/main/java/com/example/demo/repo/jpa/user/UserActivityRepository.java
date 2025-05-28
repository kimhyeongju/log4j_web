package com.example.demo.repo.jpa.user;

import com.example.demo.entity.jpa.user.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    // 사용자별 활동 조회
    Page<UserActivity> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    List<UserActivity> findByUsernameOrderByCreatedAtDesc(String username);
    
    // 활동 타입별 조회
    Page<UserActivity> findByActivityOrderByCreatedAtDesc(String activity, Pageable pageable);
    
    // 성공/실패별 조회
    Page<UserActivity> findBySuccessOrderByCreatedAtDesc(boolean success, Pageable pageable);
    
    // 로그인 실패 기록 조회 (보안 모니터링용)
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activity = 'LOGIN_ATTEMPT' " +
           "AND ua.success = false ORDER BY ua.createdAt DESC")
    Page<UserActivity> findFailedLoginAttempts(Pageable pageable);
    
    // 특정 IP의 활동 조회 (보안 분석용)
    Page<UserActivity> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);
    
    // 의심스러운 User-Agent 조회 (Log4j 공격 탐지용)
    @Query("SELECT ua FROM UserActivity ua WHERE ua.userAgent LIKE %:pattern% " +
           "ORDER BY ua.createdAt DESC")
    List<UserActivity> findByUserAgentContaining(@Param("pattern") String pattern);
    
    // 특정 기간 내 활동 조회
    @Query("SELECT ua FROM UserActivity ua WHERE ua.createdAt BETWEEN :start AND :end " +
           "ORDER BY ua.createdAt DESC")
    Page<UserActivity> findByCreatedAtBetween(@Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end, 
                                             Pageable pageable);
    
    // 사용자별 특정 기간 활동
    @Query("SELECT ua FROM UserActivity ua WHERE ua.username = :username " +
           "AND ua.createdAt BETWEEN :start AND :end ORDER BY ua.createdAt DESC")
    List<UserActivity> findByUsernameAndCreatedAtBetween(@Param("username") String username,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);
    
    // 검색 활동 조회 (Log4j 취약점 실습용)
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activity = 'SEARCH' " +
           "ORDER BY ua.createdAt DESC")
    Page<UserActivity> findSearchActivities(Pageable pageable);
    
    // 특정 검색어가 포함된 활동 (JNDI 공격 패턴 탐지)
    @Query("SELECT ua FROM UserActivity ua WHERE ua.details LIKE %:searchTerm% " +
           "ORDER BY ua.createdAt DESC")
    List<UserActivity> findByDetailsContaining(@Param("searchTerm") String searchTerm);
    
    // 통계 관련 쿼리
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.activity = :activity " +
           "AND ua.createdAt >= :since")
    long countByActivitySince(@Param("activity") String activity, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.username = :username " +
           "AND ua.success = false AND ua.createdAt >= :since")
    long countFailedAttemptsByUserSince(@Param("username") String username, 
                                       @Param("since") LocalDateTime since);
    
    // IP별 활동 수 (비정상적인 활동 탐지)
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.ipAddress = :ipAddress " +
           "AND ua.createdAt >= :since")
    long countByIpAddressSince(@Param("ipAddress") String ipAddress, 
                              @Param("since") LocalDateTime since);
    
    // 최근 로그인 성공 기록
    @Query("SELECT ua FROM UserActivity ua WHERE ua.activity = 'LOGIN_ATTEMPT' " +
           "AND ua.success = true AND ua.username = :username " +
           "ORDER BY ua.createdAt DESC")
    List<UserActivity> findRecentSuccessfulLogins(@Param("username") String username, Pageable pageable);
    
    // JNDI 패턴 탐지 (Log4j 공격 탐지)
    @Query("SELECT ua FROM UserActivity ua WHERE " +
           "(ua.details LIKE '%${jndi:%' OR ua.userAgent LIKE '%${jndi:%' " +
           "OR ua.details LIKE '%ldap://%' OR ua.userAgent LIKE '%ldap://%') " +
           "ORDER BY ua.createdAt DESC")
    List<UserActivity> findPotentialJndiAttacks();
}