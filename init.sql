-- init.sql
CREATE DATABASE IF NOT EXISTS log4j_demo;
USE log4j_demo;

-- boards 테이블 생성
CREATE TABLE IF NOT EXISTS boards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    author VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- users 테이블 생성 (만약 있다면)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 데이터 삽입
INSERT INTO users (username, password, email, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVciJC', 'admin@example.com', 'ADMIN'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVciJC', 'user1@example.com', 'USER')
ON DUPLICATE KEY UPDATE username=username;

INSERT INTO boards (title, content, author) VALUES 
('Welcome', 'Welcome to Log4j Demo Application', 'admin'),
('Test Post', 'This is a test post for demonstration', 'user1')
ON DUPLICATE KEY UPDATE title=title;