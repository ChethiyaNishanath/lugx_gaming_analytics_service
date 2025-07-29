package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.controller;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto.AnalyticsEventRequest;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto.ApiResponse;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.AnalyticsService;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.ClickHouseService;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private ClickHouseService clickHouseService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @PostMapping("/events")
    public ResponseEntity<ApiResponse<Void>> processEvents(
            @Valid @RequestBody AnalyticsEventRequest request, 
            HttpServletRequest httpRequest) {

        String clientIP = getClientIP(httpRequest);
        if (!rateLimitService.isAllowed(clientIP)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many requests from this IP"));
        }
        
        try {
            ApiResponse<Void> response = analyticsService.processEvents(request, httpRequest);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing analytics events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process events"));
        }
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(defaultValue = "24h") String timeRange,
            @RequestParam(defaultValue = "UTC") String timezone) {
        
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("timeRange", timeRange);
            dashboardData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            Map<String, Object> data = new HashMap<>();
            data.put("hourlyViews", clickHouseService.getDashboardData(timeRange));
            data.put("topPages", clickHouseService.getTopPages(timeRange));
            
            dashboardData.put("data", data);
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (SQLException e) {
            logger.error("Error fetching dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch dashboard data"));
        }
    }
    
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("realtime", true);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("metrics", clickHouseService.getRealtimeMetrics());
            
            return ResponseEntity.ok(response);
            
        } catch (SQLException e) {
            logger.error("Error fetching realtime metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch real-time metrics"));
        }
    }
    
    @GetMapping("/user-journey/{sessionId}")
    public ResponseEntity<Map<String, Object>> getUserJourney(@PathVariable String sessionId) {
        try {
            List<Map<String, Object>> journey = clickHouseService.getUserJourney(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("events", journey);
            
            // Calculate summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEvents", journey.size());
            summary.put("pagesVisited", journey.stream()
                    .filter(event -> "page_view".equals(event.get("event_type")))
                    .map(event -> event.get("page_url"))
                    .distinct()
                    .count());
            
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
            
        } catch (SQLException e) {
            logger.error("Error fetching user journey", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch user journey"));
        }
    }
    
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("pagePerformance", clickHouseService.getPerformanceMetrics());
            
            return ResponseEntity.ok(response);
            
        } catch (SQLException e) {
            logger.error("Error fetching performance metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch performance metrics"));
        }
    }
    
    @GetMapping("/database-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("current_database", clickHouseService.getCurrentDatabase());
            info.put("is_healthy", clickHouseService.isHealthy());
            info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (Exception e) {
            logger.error("Error getting database info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get database info: " + e.getMessage()));
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
