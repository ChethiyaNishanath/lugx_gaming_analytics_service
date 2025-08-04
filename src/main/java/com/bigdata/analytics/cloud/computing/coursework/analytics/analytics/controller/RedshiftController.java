package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.controller;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.RedshiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/redshift")
@ConditionalOnProperty(name = "aws.redshift.enabled", havingValue = "true", matchIfMissing = false)
public class RedshiftController {

    private static final Logger logger = LoggerFactory.getLogger(RedshiftController.class);

    @Autowired
    private RedshiftService redshiftService;

    /**
     * Get Redshift health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getRedshiftHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isHealthy = redshiftService.isHealthy();
            String currentDatabase = redshiftService.getCurrentDatabase();
            
            response.put("status", isHealthy ? "healthy" : "unhealthy");
            response.put("database", currentDatabase);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("service", "redshift");
            
            if (isHealthy) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Failed to check Redshift health", e);
            response.put("status", "unhealthy");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Execute DDL statement using Redshift Data API
     */
    @PostMapping("/ddl")
    public ResponseEntity<Map<String, Object>> executeDDL(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                response.put("error", "SQL statement is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Basic validation to allow only DDL statements
            String upperSql = sql.trim().toUpperCase();
            if (!upperSql.startsWith("CREATE") && !upperSql.startsWith("ALTER") && 
                !upperSql.startsWith("DROP") && !upperSql.startsWith("GRANT") && 
                !upperSql.startsWith("REVOKE")) {
                response.put("error", "Only DDL statements (CREATE, ALTER, DROP, GRANT, REVOKE) are allowed");
                return ResponseEntity.badRequest().body(response);
            }
            
            redshiftService.executeDDL(sql);
            
            response.put("status", "success");
            response.put("message", "DDL statement executed successfully");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to execute DDL statement", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get Redshift connection info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRedshiftInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentDatabase = redshiftService.getCurrentDatabase();
            boolean isHealthy = redshiftService.isHealthy();
            
            response.put("database", currentDatabase);
            response.put("healthy", isHealthy);
            response.put("service", "redshift");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("features", Map.of(
                "async_insert", true,
                "batch_processing", true,
                "data_api", true
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get Redshift info", e);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Initialize Redshift tables (for setup purposes)
     */
    @PostMapping("/init-tables")
    public ResponseEntity<Map<String, Object>> initializeTables() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create tables using DDL
            String pageViewTableDDL = """
                CREATE TABLE IF NOT EXISTS public.page_view_events (
                    session_id VARCHAR(255) NOT NULL,
                    user_id VARCHAR(255),
                    page_url VARCHAR(2048) NOT NULL,
                    page_title VARCHAR(1024),
                    referrer VARCHAR(2048),
                    load_time INTEGER,
                    timestamp TIMESTAMP NOT NULL,
                    user_agent VARCHAR(1024),
                    ip_address VARCHAR(45),
                    device_type VARCHAR(50),
                    browser VARCHAR(100),
                    os VARCHAR(100),
                    country VARCHAR(100),
                    city VARCHAR(100),
                    created_at TIMESTAMP DEFAULT GETDATE()
                )
                DISTSTYLE KEY
                DISTKEY (session_id)
                SORTKEY (timestamp, session_id)
                """;
                
            String clickEventTableDDL = """
                CREATE TABLE IF NOT EXISTS public.click_events (
                    session_id VARCHAR(255) NOT NULL,
                    user_id VARCHAR(255),
                    element_id VARCHAR(255),
                    element_text VARCHAR(1024),
                    page_url VARCHAR(2048) NOT NULL,
                    click_x INTEGER,
                    click_y INTEGER,
                    timestamp TIMESTAMP NOT NULL,
                    user_agent VARCHAR(1024),
                    ip_address VARCHAR(45),
                    device_type VARCHAR(50),
                    browser VARCHAR(100),
                    os VARCHAR(100),
                    country VARCHAR(100),
                    city VARCHAR(100),
                    created_at TIMESTAMP DEFAULT GETDATE()
                )
                DISTSTYLE KEY
                DISTKEY (session_id)
                SORTKEY (timestamp, session_id)
                """;
                
            String scrollEventTableDDL = """
                CREATE TABLE IF NOT EXISTS public.scroll_events (
                    session_id VARCHAR(255) NOT NULL,
                    user_id VARCHAR(255),
                    page_url VARCHAR(2048) NOT NULL,
                    scroll_depth INTEGER,
                    scroll_percentage DECIMAL(5,2),
                    timestamp TIMESTAMP NOT NULL,
                    user_agent VARCHAR(1024),
                    ip_address VARCHAR(45),
                    device_type VARCHAR(50),
                    browser VARCHAR(100),
                    os VARCHAR(100),
                    country VARCHAR(100),
                    city VARCHAR(100),
                    created_at TIMESTAMP DEFAULT GETDATE()
                )
                DISTSTYLE KEY
                DISTKEY (session_id)
                SORTKEY (timestamp, session_id)
                """;
                
            String sessionEventTableDDL = """
                CREATE TABLE IF NOT EXISTS public.session_events (
                    session_id VARCHAR(255) NOT NULL,
                    user_id VARCHAR(255),
                    event_type VARCHAR(50) NOT NULL,
                    page_count INTEGER,
                    timestamp TIMESTAMP NOT NULL,
                    user_agent VARCHAR(1024),
                    ip_address VARCHAR(45),
                    device_type VARCHAR(50),
                    browser VARCHAR(100),
                    os VARCHAR(100),
                    country VARCHAR(100),
                    city VARCHAR(100),
                    created_at TIMESTAMP DEFAULT GETDATE()
                )
                DISTSTYLE KEY
                DISTKEY (session_id)
                SORTKEY (timestamp, session_id)
                """;
            
            // Execute DDL statements
            redshiftService.executeDDL(pageViewTableDDL);
            redshiftService.executeDDL(clickEventTableDDL);
            redshiftService.executeDDL(scrollEventTableDDL);
            redshiftService.executeDDL(sessionEventTableDDL);
            
            response.put("status", "success");
            response.put("message", "Redshift tables initialized successfully");
            response.put("tables_created", new String[]{
                "page_view_events", 
                "click_events", 
                "scroll_events", 
                "session_events"
            });
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to initialize Redshift tables", e);
            response.put("status", "error");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
