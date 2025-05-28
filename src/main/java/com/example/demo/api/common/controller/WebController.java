package com.example.demo.api.common.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

/**
 * Thymeleaf 페이지를 서빙하는 웹 컨트롤러
 * 물류센터 테마의 프론트엔드 페이지들을 제공
 */
@Controller
@Log4j2
public class WebController {

    /**
     * 메인 홈페이지 (대시보드)
     */
    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 홈페이지 접근 로깅
        log.info("Main dashboard accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("currentPage", "dashboard");
        
        return "index";
    }

    /**
     * 입고 관리 페이지 (게시판)
     */
    @GetMapping("/warehouse")
    public String warehousePage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 입고 관리 페이지 접근 로깅
        log.info("Warehouse management page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "입고 관리");
        model.addAttribute("currentPage", "warehouse");
        
        return "board-list";
    }

    /**
     * 출고 관리 페이지
     */
    @GetMapping("/shipping")
    public String shippingPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 출고 관리 페이지 접근 로깅
        log.info("Shipping management page accessed from IP: {}", getClientIpAddress(request));
        
        model.addAttribute("pageTitle", "출고 관리");
        model.addAttribute("currentPage", "shipping");
        
        return "shipping";
    }

    /**
     * 재고 조회 페이지 (검색 기능)
     */
    @GetMapping("/inventory")
    public String inventoryPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 재고 조회 페이지 접근 로깅
        log.info("Inventory search page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "재고 조회");
        model.addAttribute("currentPage", "inventory");
        
        return "inventory";
    }

    /**
     * 배송 추적 페이지
     */
    @GetMapping("/tracking")
    public String trackingPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 배송 추적 페이지 접근 로깅
        log.info("Tracking page accessed from IP: {}", getClientIpAddress(request));
        
        model.addAttribute("pageTitle", "배송 추적");
        model.addAttribute("currentPage", "tracking");
        
        return "tracking";
    }

    /**
     * 취약점 실습 페이지
     */
    @GetMapping("/vulnerable")
    public String vulnerablePage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 취약점 실습 페이지 접근 로깅
        log.warn("Vulnerability demo page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "취약점 실습");
        model.addAttribute("currentPage", "vulnerable");
        
        return "vulnerable";
    }

    /**
     * 직원 관리 페이지 (관리자)
     */
    @GetMapping("/admin/users")
    public String adminUsersPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 관리자 페이지 접근 로깅
        log.info("Admin users page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "직원 관리");
        model.addAttribute("currentPage", "admin-users");
        
        return "admin/users";
    }

    /**
     * 시스템 로그 페이지 (관리자)
     */
    @GetMapping("/admin/logs")
    public String adminLogsPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 시스템 로그 페이지 접근 로깅
        log.info("Admin logs page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "시스템 로그");
        model.addAttribute("currentPage", "admin-logs");
        
        return "admin/logs";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String registerPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 회원가입 페이지 접근 로깅
        log.info("Registration page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("currentPage", "register");
        
        return "register";
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 로그인 페이지 접근 로깅
        log.info("Login page accessed from IP: {}, User-Agent: {}", 
                getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("currentPage", "login");
        
        return "login";
    }

    /**
     * 게시글 상세 페이지
     */
    @GetMapping("/warehouse/{id}")
    public String warehouseDetailPage(@PathVariable Long id, Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 게시글 상세 접근 로깅
        log.info("Warehouse item detail accessed - ID: {}, IP: {}, User-Agent: {}", 
                id, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        model.addAttribute("pageTitle", "입고 상세");
        model.addAttribute("currentPage", "warehouse");
        model.addAttribute("itemId", id);
        
        return "warehouse-detail";
    }

    /**
     * 🚨 취약점 실습용 파라미터 테스트 페이지
     */
    @GetMapping("/test")
    public String testPage(Model model, HttpServletRequest request) {
        String param = request.getParameter("param");
        
        // 🚨 Log4j 취약점: 파라미터를 직접 로깅
        log.info("Test page accessed with parameter: {}", param);
        
        model.addAttribute("pageTitle", "테스트");
        model.addAttribute("testParam", param);
        
        return "test";
    }

    /**
     * 🚨 취약점 실습용 헤더 테스트
     */
    @GetMapping("/header-test")
    public String headerTestPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 모든 헤더 로깅
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            log.info("Header test - {}: {}", headerName, headerValue);
        });
        
        model.addAttribute("pageTitle", "헤더 테스트");
        return "header-test";
    }

    /**
     * 에러 페이지 핸들링
     */
    @GetMapping("/error")
    public String errorPage(Model model, HttpServletRequest request) {
        // 🚨 Log4j 취약점: 에러 페이지 정보 로깅
        String errorMsg = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        log.error("Error page accessed - URI: {}, Message: {}, IP: {}", 
                requestUri, errorMsg, getClientIpAddress(request));
        
        model.addAttribute("pageTitle", "오류");
        model.addAttribute("errorMessage", errorMsg);
        
        return "error";
    }

    /**
     * 클라이언트 IP 주소 추출
     */
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