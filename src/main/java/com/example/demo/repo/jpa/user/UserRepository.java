package com.example.demo.repo.jpa.user;

import com.example.demo.entity.jpa.user.UserEntity;
import com.example.demo.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 기본 조회 메서드
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // 활성 사용자 조회
    List<UserEntity> findByEnabledTrue();
    
    Page<UserEntity> findByEnabledTrue(Pageable pageable);
    
    // 역할별 조회
    List<UserEntity> findByRole(UserRole role);
    
    Page<UserEntity> findByRole(UserRole role, Pageable pageable);
    
    // 검색 기능 (Log4j 취약점 실습용)
    @Query("SELECT u FROM UserEntity u WHERE " +
           "u.username LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.nickname LIKE %:keyword%")
    Page<UserEntity> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 최근 가입 사용자
    @Query("SELECT u FROM UserEntity u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<UserEntity> findRecentUsers(@Param("since") LocalDateTime since);
    
    // 활성 사용자 수 카운트
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.enabled = true")
    long countActiveUsers();
    
    // 역할별 사용자 수
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.role = :role AND u.enabled = true")
    long countByRoleAndEnabled(@Param("role") UserRole role);
    
    // 사용자명과 이메일로 조회 (로그인 실패 로깅용)
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username OR u.email = :username")
    Optional<UserEntity> findByUsernameOrEmail(@Param("username") String username);
}