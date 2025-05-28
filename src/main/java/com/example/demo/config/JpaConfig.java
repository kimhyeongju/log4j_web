package com.example.demo.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.example.demo.repo")
@EnableTransactionManagement
@Log4j2
public class JpaConfig {

    @PostConstruct
    public void init() {
        // 🚨 Log4j 취약점: JPA 설정 초기화 로깅
        log.info("JPA configuration initialized for Log4j vulnerability demo");
        log.debug("JPA Auditing enabled for entity timestamp management");
        log.debug("JPA Repositories base package: com.example.demo.repo");
    }
}