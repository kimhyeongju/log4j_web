package com.example.demo.api.board.controller;

import com.example.demo.api.board.dto.BoardCreateDto;
import com.example.demo.api.board.dto.BoardUpdateDto;
import com.example.demo.api.board.service.BoardService;
import com.example.demo.entity.jpa.board.BoardEntity;
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
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Log4j2
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 생성
     * 🚨 Log4j 취약점: 게시글 제목과 내용 로깅
     */
    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody BoardCreateDto dto, 
                                       HttpServletRequest request) {
        try {
            // 🚨 Log4j 취약점: 게시글 생성 요청 로깅
            log.info("Post creation request - Title: {}, Content length: {}, Author: {}, IP: {}", 
                    dto.getTitle(), dto.getContent().length(), dto.getAuthor(), getClientIpAddress(request));
            
            BoardEntity board = boardService.createPost(
                    dto.getTitle(), 
                    dto.getContent(), 
                    dto.getAuthor(), 
                    request
            );
            
            return ResponseEntity.ok().body("Post created successfully with ID: " + board.getId());
        } catch (Exception e) {
            // 🚨 Log4j 취약점: 에러 로그에 사용자 입력 포함
            log.error("Post creation failed - Title: {}, Author: {}, Error: {}", 
                    dto.getTitle(), dto.getAuthor(), e.getMessage());
            return ResponseEntity.badRequest().body("Post creation failed: " + e.getMessage());
        }
    }

    /**
     * 게시글 상세 조회
     * 🚨 Log4j 취약점: 조회 시 User-Agent 로깅
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoardEntity> getPost(@PathVariable Long id, HttpServletRequest request) {
        try {
            BoardEntity board = boardService.getPostById(id, request);
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            log.error("Post not found - ID: {}, IP: {}", id, getClientIpAddress(request));
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<BoardEntity>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 목록 조회 로그
        log.info("Board list request - Page: {}, Size: {}, IP: {}, User-Agent: {}", 
                page, size, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.getAllPosts(pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 🚨 게시글 검색 (Log4j 취약점의 주요 공격 벡터)
     * 검색어에 JNDI 문자열 삽입 가능한 엔드포인트
     */
    @GetMapping("/search")
    public ResponseEntity<Page<BoardEntity>> searchPosts(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 검색 키워드를 직접 로깅 (JNDI Injection 주요 벡터)
        log.info("Board search request - Keyword: '{}', Page: {}, Size: {}", keyword, page, size);
        log.debug("Search details - Raw keyword: '{}', IP: {}, User-Agent: {}", 
                keyword, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.searchPosts(keyword, request, pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 🚨 제목으로만 검색
     */
    @GetMapping("/search/title")
    public ResponseEntity<Page<BoardEntity>> searchByTitle(
            @RequestParam("q") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 제목 검색 로깅
        log.info("Title search - Query: '{}' from IP: {}", title, getClientIpAddress(request));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.searchByTitle(title, request, pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 🚨 내용으로만 검색
     */
    @GetMapping("/search/content")
    public ResponseEntity<Page<BoardEntity>> searchByContent(
            @RequestParam("q") String content,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 내용 검색 로깅
        log.info("Content search - Query: '{}' from IP: {}", content, getClientIpAddress(request));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.searchByContent(content, request, pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 게시글 수정
     * 🚨 Log4j 취약점: 수정 내용 로깅
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                       @Valid @RequestBody BoardUpdateDto dto,
                                       HttpServletRequest request) {
        try {
            // 🚨 Log4j 취약점: 수정 요청 로깅
            log.info("Post update request - ID: {}, New Title: {}, Author: {}, IP: {}", 
                    id, dto.getTitle(), dto.getAuthor(), getClientIpAddress(request));
            
            BoardEntity board = boardService.updatePost(
                    id, 
                    dto.getTitle(), 
                    dto.getContent(), 
                    dto.getAuthor(), 
                    request
            );
            
            return ResponseEntity.ok("Post updated successfully");
        } catch (Exception e) {
            log.error("Post update failed - ID: {}, Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                       @RequestParam String author,
                                       HttpServletRequest request) {
        try {
            // 🚨 Log4j 취약점: 삭제 요청 로깅
            log.info("Post deletion request - ID: {}, Author: {}, IP: {}", 
                    id, author, getClientIpAddress(request));
            
            boardService.deletePost(id, author, request);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (Exception e) {
            log.error("Post deletion failed - ID: {}, Error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        }
    }

    /**
     * 인기 게시글 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<BoardEntity>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.getPopularPosts(pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 특정 사용자의 게시글 조회
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<BoardEntity>> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        // 🚨 Log4j 취약점: 사용자별 게시글 조회 로깅
        log.info("User posts request - Username: {}, IP: {}", username, getClientIpAddress(request));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardEntity> boards = boardService.getUserPosts(username, pageable);
        
        return ResponseEntity.ok(boards);
    }

    /**
     * 게시글 통계 조회
     */
    @GetMapping("/stats/total")
    public ResponseEntity<?> getTotalPostCount() {
        long count = boardService.getTotalPostCount();
        return ResponseEntity.ok().body("Total posts: " + count);
    }

    /**
     * 사용자별 게시글 수 조회
     */
    @GetMapping("/stats/user/{username}")
    public ResponseEntity<?> getUserPostCount(@PathVariable String username) {
        long count = boardService.getUserPostCount(username);
        return ResponseEntity.ok().body("Posts by " + username + ": " + count);
    }

    /**
     * 🔍 보안 모니터링: 의심스러운 검색 활동 탐지 (관리자용)
     */
    @PostMapping("/security/monitor")
    public ResponseEntity<?> monitorSuspiciousActivity(HttpServletRequest request) {
        // 🚨 Log4j 취약점: 보안 모니터링 요청 로깅
        log.info("Security monitoring requested from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        boardService.monitorSuspiciousActivity();
        return ResponseEntity.ok("Security monitoring completed. Check logs for details.");
    }

    /**
     * 🚨 특별한 Log4j 취약점 실습 엔드포인트
     * 게시글 제목 검증 (의도적으로 취약하게 작성)
     */
    @GetMapping("/validate-title")
    public ResponseEntity<?> validateTitle(@RequestParam String title, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 제목 검증 시 입력값 직접 로깅
        log.info("Title validation request: {}", title);
        log.debug("Validating title: '{}' from IP: {} with User-Agent: {}", 
                title, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        if (title.length() > 200) {
            log.warn("Title too long: {}", title);
            return ResponseEntity.badRequest().body("Title too long: " + title);
        }
        
        if (title.trim().isEmpty()) {
            log.warn("Empty title received from IP: {}", getClientIpAddress(request));
            return ResponseEntity.badRequest().body("Title cannot be empty");
        }
        
        log.info("Title validation passed: {}", title);
        return ResponseEntity.ok("Title is valid: " + title);
    }

    /**
     * 🚨 디버그용 요청 정보 로깅 엔드포인트
     */
    @PostMapping("/debug/request")
    public ResponseEntity<?> debugRequest(@RequestBody String requestBody, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 요청 본문과 헤더 정보를 모두 로깅
        log.debug("Request body: {}", requestBody);
        log.debug("Request from IP: {}, Method: {}, URI: {}", 
                getClientIpAddress(request), request.getMethod(), request.getRequestURI());
        
        // 모든 헤더 로깅
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.debug("Request header - {}: {}", headerName, headerValue);
        });
        
        return ResponseEntity.ok("Request logged for debugging");
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