package com.example.demo.api.board.service;


import com.example.demo.entity.jpa.board.BoardEntity;
import com.example.demo.entity.jpa.user.UserActivity;
import com.example.demo.entity.jpa.user.UserEntity;
import com.example.demo.repo.jpa.board.BoardRepository;
import com.example.demo.repo.jpa.user.UserActivityRepository;
import com.example.demo.repo.jpa.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;

    @Transactional
    public BoardEntity createPost(String title, String content, String username, HttpServletRequest request) {
        UserEntity author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        BoardEntity board = BoardEntity.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        BoardEntity savedBoard = boardRepository.save(board);

        // 🚨 Log4j 취약점: 게시글 제목과 내용을 직접 로깅
        log.info("New post created - Title: {}, Content preview: {}, Author: {} from IP: {}", 
                title, 
                content.length() > 100 ? content.substring(0, 100) + "..." : content,
                username, 
                getClientIpAddress(request));

        // 활동 로그 저장
        UserActivity activity = UserActivity.builder()
                .username(username)
                .activity("POST_CREATED")
                .ipAddress(getClientIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .details("Post ID: " + savedBoard.getId() + ", Title: " + title)
                .success(true)
                .build();
        userActivityRepository.save(activity);

        return savedBoard;
    }

    @Transactional
    public BoardEntity getPostById(Long id, HttpServletRequest request) {
        BoardEntity board = boardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        // 조회수 증가
        boardRepository.increaseViewCount(id);

        // 🚨 Log4j 취약점: 게시글 조회 로그 (User-Agent 포함)
        log.info("Post viewed - ID: {}, Title: {}, Viewer IP: {}, User-Agent: {}", 
                id, board.getTitle(), getClientIpAddress(request), request.getHeader("User-Agent"));

        // 활동 로그 저장
        UserActivity activity = UserActivity.boardAccess(
                getCurrentUsername(request), 
                getClientIpAddress(request), 
                request.getHeader("User-Agent"), 
                board.getTitle()
        );
        userActivityRepository.save(activity);

        return board;
    }

    public Page<BoardEntity> getAllPosts(Pageable pageable) {
        return boardRepository.findAllNotDeleted(pageable);
    }

    public Page<BoardEntity> searchPosts(String keyword, HttpServletRequest request, Pageable pageable) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String currentUser = getCurrentUsername(request);

        // 🚨 Log4j 취약점: 검색 키워드를 직접 로깅 (JNDI Injection의 주요 공격 벡터)
        log.info("Board search performed - Keyword: {}, User: {}, IP: {}, User-Agent: {}", 
                keyword, currentUser, clientIp, userAgent);

        // 🚨 추가 취약점: 검색 통계를 위한 상세 로깅
        log.debug("Search details - Raw keyword: '{}', Length: {}, User: {}", 
                keyword, keyword != null ? keyword.length() : 0, currentUser);

        // 검색 활동 기록
        UserActivity searchActivity = UserActivity.searchActivity(currentUser, clientIp, userAgent, keyword);
        userActivityRepository.save(searchActivity);

        return boardRepository.findByTitleOrContentContaining(keyword, pageable);
    }

    public Page<BoardEntity> searchByTitle(String title, HttpServletRequest request, Pageable pageable) {
        // 🚨 Log4j 취약점: 제목 검색 로깅
        log.info("Title search - Keyword: {}, IP: {}", title, getClientIpAddress(request));
        
        return boardRepository.findByTitleContaining(title, pageable);
    }

    public Page<BoardEntity> searchByContent(String content, HttpServletRequest request, Pageable pageable) {
        // 🚨 Log4j 취약점: 내용 검색 로깅
        log.info("Content search - Keyword: {}, IP: {}", content, getClientIpAddress(request));
        
        return boardRepository.findByContentContaining(content, pageable);
    }

    @Transactional
    public BoardEntity updatePost(Long id, String title, String content, String username, HttpServletRequest request) {
        BoardEntity board = boardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!board.isOwner(user)) {
            // 🚨 Log4j 취약점: 권한 없는 수정 시도 로깅
            log.warn("Unauthorized post modification attempt - Post ID: {}, User: {}, IP: {}, User-Agent: {}", 
                    id, username, getClientIpAddress(request), request.getHeader("User-Agent"));
            throw new RuntimeException("Access denied");
        }

        board.updatePost(title, content);
        BoardEntity updatedBoard = boardRepository.save(board);

        // 🚨 Log4j 취약점: 수정된 내용을 로깅
        log.info("Post updated - ID: {}, New Title: {}, Updated by: {}, IP: {}", 
                id, title, username, getClientIpAddress(request));

        return updatedBoard;
    }

    @Transactional
    public void deletePost(Long id, String username, HttpServletRequest request) {
        BoardEntity board = boardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!board.isOwner(user)) {
            // 🚨 Log4j 취약점: 권한 없는 삭제 시도 로깅
            log.warn("Unauthorized post deletion attempt - Post ID: {}, User: {}, IP: {}", 
                    id, username, getClientIpAddress(request));
            throw new RuntimeException("Access denied");
        }

        board.delete();
        boardRepository.save(board);

        // 🚨 Log4j 취약점: 삭제 로그
        log.info("Post deleted - ID: {}, Title: {}, Deleted by: {}, IP: {}", 
                id, board.getTitle(), username, getClientIpAddress(request));
    }

    public Page<BoardEntity> getPopularPosts(Pageable pageable) {
        return boardRepository.findPopularPosts(pageable);
    }

    public Page<BoardEntity> getUserPosts(String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return boardRepository.findByAuthorAndDeletedFalse(user, pageable);
    }

    // 통계 관련 메서드
    public long getTotalPostCount() {
        return boardRepository.countNotDeleted();
    }

    public long getUserPostCount(String username) {
        return boardRepository.countByAuthorUsername(username);
    }

    // 🔍 보안 모니터링: 의심스러운 검색 패턴 탐지
    public void monitorSuspiciousActivity() {
        // JNDI 패턴이 포함된 검색 활동 모니터링
        var suspiciousSearches = userActivityRepository.findByDetailsContaining("${jndi:");
        
        if (!suspiciousSearches.isEmpty()) {
            log.error("SECURITY ALERT: Potential JNDI injection in search detected! Count: {}", 
                    suspiciousSearches.size());
            
            suspiciousSearches.forEach(activity -> 
                log.error("Suspicious search - User: {}, IP: {}, Search term: {}", 
                        activity.getUsername(), activity.getIpAddress(), activity.getDetails())
            );
        }

        // 비정상적인 User-Agent 패턴 모니터링
        var ldapPatterns = userActivityRepository.findByUserAgentContaining("ldap://");
        if (!ldapPatterns.isEmpty()) {
            log.error("SECURITY ALERT: LDAP patterns detected in User-Agent! Count: {}", 
                    ldapPatterns.size());
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
