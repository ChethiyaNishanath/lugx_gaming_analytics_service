package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.controller;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.ClickHouseService;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.RedshiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired
    private ClickHouseService clickHouseService;
    
    @Autowired(required = false)
    private RedshiftService redshiftService;
    
    @Value("${aws.redshift.enabled:false}")
    private boolean redshiftEnabled;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> databases = new HashMap<>();
        
        try {
            // Check ClickHouse health
            boolean clickHouseHealthy = clickHouseService.isHealthy();
            databases.put("clickhouse", Map.of(
                "status", clickHouseHealthy ? "healthy" : "unhealthy",
                "database", clickHouseService.getCurrentDatabase()
            ));
            
            // Check Redshift health if enabled
            boolean redshiftHealthy = true;
            if (redshiftEnabled && redshiftService != null) {
                redshiftHealthy = redshiftService.isHealthy();
                databases.put("redshift", Map.of(
                    "status", redshiftHealthy ? "healthy" : "unhealthy",
                    "database", redshiftService.getCurrentDatabase(),
                    "enabled", true
                ));
            } else {
                databases.put("redshift", Map.of(
                    "status", "disabled",
                    "enabled", false
                ));
            }
            
            boolean overallHealthy = clickHouseHealthy && redshiftHealthy;
            
            response.put("status", overallHealthy ? "healthy" : "unhealthy");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("service", "analytics");
            response.put("databases", databases);
            response.put("features", Map.of(
                "clickhouse", true,
                "redshift", redshiftEnabled,
                "s3_export", true
            ));
            
            if (overallHealthy) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("databases", databases);
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
