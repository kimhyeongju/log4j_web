package com.example.demo.repo.jpa.board;

import com.example.demo.entity.jpa.board.BoardEntity;
import com.example.demo.entity.jpa.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 기본 조회 (삭제되지 않은 게시글만)
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false ORDER BY b.createdAt DESC")
    Page<BoardEntity> findAllNotDeleted(Pageable pageable);
    
    Optional<BoardEntity> findByIdAndDeletedFalse(Long id);
    
    // 작성자별 조회
    Page<BoardEntity> findByAuthorAndDeletedFalse(UserEntity author, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.author = :author AND b.deleted = false")
    long countByAuthorAndNotDeleted(@Param("author") UserEntity author);
    
    // 검색 기능 (Log4j 취약점 실습의 핵심 부분)
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false AND " +
           "(b.title LIKE %:keyword% OR b.content LIKE %:keyword%) " +
           "ORDER BY b.createdAt DESC")
    Page<BoardEntity> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 제목으로만 검색
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false AND " +
           "b.title LIKE %:title% ORDER BY b.createdAt DESC")
    Page<BoardEntity> findByTitleContaining(@Param("title") String title, Pageable pageable);
    
    // 내용으로만 검색
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false AND " +
           "b.content LIKE %:content% ORDER BY b.createdAt DESC")
    Page<BoardEntity> findByContentContaining(@Param("content") String content, Pageable pageable);
    
    // 인기 게시글 (조회수 기준)
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false " +
           "ORDER BY b.viewCount DESC, b.createdAt DESC")
    Page<BoardEntity> findPopularPosts(Pageable pageable);
    
    // 최근 게시글
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false AND b.createdAt >= :since " +
           "ORDER BY b.createdAt DESC")
    List<BoardEntity> findRecentPosts(@Param("since") LocalDateTime since);
    
    // 조회수 증가
    @Modifying
    @Query("UPDATE BoardEntity b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void increaseViewCount(@Param("id") Long id);
    
    // 통계 관련
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.deleted = false")
    long countNotDeleted();
    
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.deleted = false AND b.createdAt >= :since")
    long countRecentPosts(@Param("since") LocalDateTime since);
    
    // 특정 사용자의 게시글 수
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.author.username = :username AND b.deleted = false")
    long countByAuthorUsername(@Param("username") String username);
    
    // 조회수가 높은 게시글 (특정 개수)
    @Query("SELECT b FROM BoardEntity b WHERE b.deleted = false " +
           "ORDER BY b.viewCount DESC")
    List<BoardEntity> findTopViewedPosts(Pageable pageable);
}