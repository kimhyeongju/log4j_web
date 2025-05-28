package com.example.demo.entity.jpa.board;

import com.example.demo.entity.jpa.common.BaseEntity;
import com.example.demo.entity.jpa.user.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public BoardEntity(String title, String content, UserEntity author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = 0L;
        this.deleted = false;
    }

    // 비즈니스 메서드
    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.deleted = true;
    }

    public void restore() {
        this.deleted = false;
    }

    public boolean isOwner(UserEntity user) {
        return this.author.getId().equals(user.getId());
    }
}