package com.example.demo.entity.jpa.user;

import com.example.demo.entity.jpa.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String activity;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private boolean success;

    @Builder
    public UserActivity(String username, String activity, String ipAddress, 
                       String userAgent, String details, boolean success) {
        this.username = username;
        this.activity = activity;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.success = success;
    }

    // 정적 팩토리 메서드
    public static UserActivity loginAttempt(String username, String ipAddress, 
                                          String userAgent, boolean success) {
        return UserActivity.builder()
                .username(username)
                .activity("LOGIN_ATTEMPT")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .build();
    }

    public static UserActivity boardAccess(String username, String ipAddress, 
                                         String userAgent, String boardTitle) {
        return UserActivity.builder()
                .username(username)
                .activity("BOARD_ACCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details("Board: " + boardTitle)
                .success(true)
                .build();
    }

    public static UserActivity searchActivity(String username, String ipAddress, 
                                            String userAgent, String searchTerm) {
        return UserActivity.builder()
                .username(username)
                .activity("SEARCH")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details("Search term: " + searchTerm)
                .success(true)
                .build();
    }
}