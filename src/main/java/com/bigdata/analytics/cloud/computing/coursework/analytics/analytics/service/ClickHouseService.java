package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClickHouseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseService.class);
    
    @Autowired
    private Connection clickHouseConnection;
    
    public void insertPageViewEvents(List<PageViewEvent> events) throws SQLException {
        String sql = """
            INSERT INTO analytics.page_view_events (
                event_id, session_id, user_id, page_url, page_title, timestamp,\s
                server_timestamp, user_agent, country, city, browser, browser_version,\s
                os, os_version, device_type, screen_width, screen_height, viewport_width,\s
                viewport_height, referrer, page_load_time, time_on_page, is_bounce,\s
                entry_page, exit_page
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           \s""";
        
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            for (PageViewEvent event : events) {
                stmt.setString(1, event.getEventId());
                stmt.setString(2, event.getSessionId());
                stmt.setString(3, event.getUserId());
                stmt.setString(4, event.getPageUrl());
                stmt.setString(5, event.getPageTitle());
                stmt.setString(6, event.getTimestamp());
                stmt.setString(7, event.getServerTimestamp());
                stmt.setString(8, event.getUserAgent());
                stmt.setString(9, event.getCountry());
                stmt.setString(10, event.getCity());
                stmt.setString(11, event.getBrowser());
                stmt.setString(12, event.getBrowserVersion());
                stmt.setString(13, event.getOs());
                stmt.setString(14, event.getOsVersion());
                stmt.setString(15, event.getDeviceType());
                stmt.setInt(16, event.getScreenWidth());
                stmt.setInt(17, event.getScreenHeight());
                stmt.setInt(18, event.getViewportWidth());
                stmt.setInt(19, event.getViewportHeight());
                stmt.setString(20, event.getReferrer());
                stmt.setInt(21, event.getPageLoadTime());
                stmt.setInt(22, event.getTimeOnPage());
                stmt.setBoolean(23, event.getIsBounce());
                stmt.setBoolean(24, event.getEntryPage());
                stmt.setBoolean(25, event.getExitPage());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Successfully inserted {} page view events into ClickHouse", events.size());
        }
    }

    public void insertClickEvents(List<ClickEvent> events) throws SQLException {
        String sql = """
            INSERT INTO analytics.click_events (
                event_id, session_id, user_id, page_url, timestamp, server_timestamp,\s
                user_agent, country, city, browser, browser_version, os, os_version,\s
                device_type, screen_width, screen_height, viewport_width, viewport_height,\s
                referrer, click_x, click_y, element_id, element_class, element_tag,\s
                element_text, click_type, is_double_click
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           \s""";
        
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            for (ClickEvent event : events) {
                stmt.setString(1, event.getEventId());
                stmt.setString(2, event.getSessionId());
                stmt.setString(3, event.getUserId());
                stmt.setString(4, event.getPageUrl());
                stmt.setString(5, event.getTimestamp());
                stmt.setString(6, event.getServerTimestamp());
                stmt.setString(7, event.getUserAgent());
                stmt.setString(8, event.getCountry());
                stmt.setString(9, event.getCity());
                stmt.setString(10, event.getBrowser());
                stmt.setString(11, event.getBrowserVersion());
                stmt.setString(12, event.getOs());
                stmt.setString(13, event.getOsVersion());
                stmt.setString(14, event.getDeviceType());
                stmt.setInt(15, event.getScreenWidth());
                stmt.setInt(16, event.getScreenHeight());
                stmt.setInt(17, event.getViewportWidth());
                stmt.setInt(18, event.getViewportHeight());
                stmt.setString(19, event.getReferrer());
                stmt.setInt(20, event.getClickX());
                stmt.setInt(21, event.getClickY());
                stmt.setString(22, event.getElementId());
                stmt.setString(23, event.getElementClass());
                stmt.setString(24, event.getElementTag());
                stmt.setString(25, event.getElementText());
                stmt.setString(26, event.getClickType());
                stmt.setBoolean(27, event.getIsDoubleClick());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Successfully inserted {} click events into ClickHouse", events.size());
        }
    }

    public void insertScrollEvents(List<ScrollEvent> events) throws SQLException {
        String sql = """
            INSERT INTO analytics.scroll_events (
                event_id, session_id, user_id, page_url, timestamp, server_timestamp,\s
                user_agent, country, city, browser, browser_version, os, os_version,\s
                device_type, screen_width, screen_height, viewport_width, viewport_height,\s
                referrer, scroll_depth, max_scroll_depth, scroll_percentage, scroll_direction,\s
                scroll_speed, time_to_scroll
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           \s""";
        
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            for (ScrollEvent event : events) {
                stmt.setString(1, event.getEventId());
                stmt.setString(2, event.getSessionId());
                stmt.setString(3, event.getUserId());
                stmt.setString(4, event.getPageUrl());
                stmt.setString(5, event.getTimestamp());
                stmt.setString(6, event.getServerTimestamp());
                stmt.setString(7, event.getUserAgent());
                stmt.setString(8, event.getCountry());
                stmt.setString(9, event.getCity());
                stmt.setString(10, event.getBrowser());
                stmt.setString(11, event.getBrowserVersion());
                stmt.setString(12, event.getOs());
                stmt.setString(13, event.getOsVersion());
                stmt.setString(14, event.getDeviceType());
                stmt.setInt(15, event.getScreenWidth());
                stmt.setInt(16, event.getScreenHeight());
                stmt.setInt(17, event.getViewportWidth());
                stmt.setInt(18, event.getViewportHeight());
                stmt.setString(19, event.getReferrer());
                stmt.setInt(20, event.getScrollDepth());
                stmt.setInt(21, event.getMaxScrollDepth());
                stmt.setDouble(22, event.getScrollPercentage());
                stmt.setString(23, event.getScrollDirection());
                stmt.setInt(24, event.getScrollSpeed());
                stmt.setInt(25, event.getTimeToScroll());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Successfully inserted {} scroll events into ClickHouse", events.size());
        }
    }

    public void insertSessionEvents(List<SessionEvent> events) throws SQLException {
        String sql = """
            INSERT INTO analytics.session_events (
                event_id, session_id, user_id, page_url, timestamp, server_timestamp, 
                user_agent, country, city, browser, browser_version, os, os_version, 
                device_type, screen_width, screen_height, viewport_width, viewport_height, 
                referrer, event_type, session_duration, page_count, total_clicks, 
                total_scroll_depth, is_bounce, entry_page, exit_page
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            for (SessionEvent event : events) {
                stmt.setString(1, event.getEventId());
                stmt.setString(2, event.getSessionId());
                stmt.setString(3, event.getUserId());
                stmt.setString(4, event.getPageUrl());
                stmt.setString(5, event.getTimestamp());
                stmt.setString(6, event.getServerTimestamp());
                stmt.setString(7, event.getUserAgent());
                stmt.setString(8, event.getCountry());
                stmt.setString(9, event.getCity());
                stmt.setString(10, event.getBrowser());
                stmt.setString(11, event.getBrowserVersion());
                stmt.setString(12, event.getOs());
                stmt.setString(13, event.getOsVersion());
                stmt.setString(14, event.getDeviceType());
                stmt.setInt(15, event.getScreenWidth());
                stmt.setInt(16, event.getScreenHeight());
                stmt.setInt(17, event.getViewportWidth());
                stmt.setInt(18, event.getViewportHeight());
                stmt.setString(19, event.getReferrer());
                stmt.setString(20, event.getEventType());
                stmt.setInt(21, event.getSessionDuration());
                stmt.setInt(22, event.getPageCount());
                stmt.setInt(23, event.getTotalClicks());
                stmt.setInt(24, event.getTotalScrollDepth());
                stmt.setBoolean(25, event.getIsBounce());
                stmt.setString(26, event.getEntryPage());
                stmt.setString(27, event.getExitPage());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            logger.info("Successfully inserted {} session events into ClickHouse", events.size());
        }
    }
    
    public boolean isHealthy() {
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement("SELECT 1")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            logger.error("ClickHouse health check failed", e);
            return false;
        }
    }

    public String getCurrentDatabase() {
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement("SELECT currentDatabase()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String currentDb = rs.getString(1);
                logger.info("Currently connected to database: {}", currentDb);
                return currentDb;
            }
            return "unknown";
        } catch (SQLException e) {
            logger.error("Failed to get current database", e);
            return "error";
        }
    }

    public List<Map<String, Object>> getPageViewAnalytics(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                toStartOfHour(timestamp) as hour,
                count() as page_views,
                uniq(session_id) as unique_sessions,
                uniq(user_id) as unique_users,
                avg(page_load_time) as avg_load_time,
                avg(time_on_page) as avg_time_on_page
            FROM analytics.page_view_events\s
            WHERE timestamp >= now() - INTERVAL %s
            GROUP BY hour\s
            ORDER BY hour
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    public List<Map<String, Object>> getTopPages(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                page_url,
                page_title,
                count() as views,
                uniq(session_id) as unique_sessions,
                avg(page_load_time) as avg_load_time,
                avg(time_on_page) as avg_time_on_page,
                countIf(is_bounce = 1) as bounces,
                (countIf(is_bounce = 1) / count()) * 100 as bounce_rate
            FROM analytics.page_view_events\s
            WHERE timestamp >= now() - INTERVAL %s
            GROUP BY page_url, page_title\s
            ORDER BY views DESC\s
            LIMIT 20
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    public List<Map<String, Object>> getClickAnalytics(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                page_url,
                element_tag,
                element_id,
                element_class,
                count() as clicks,
                uniq(session_id) as unique_sessions,
                avg(click_x) as avg_x,
                avg(click_y) as avg_y,
                countIf(is_double_click = 1) as double_clicks
            FROM analytics.click_events\s
            WHERE timestamp >= now() - INTERVAL %s
            GROUP BY page_url, element_tag, element_id, element_class
            HAVING clicks > 5
            ORDER BY clicks DESC
            LIMIT 50
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    public List<Map<String, Object>> getScrollAnalytics(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                page_url,
                avg(scroll_percentage) as avg_scroll_percentage,
                avg(max_scroll_depth) as avg_max_scroll_depth,
                avg(scroll_speed) as avg_scroll_speed,
                count() as scroll_events,
                uniq(session_id) as unique_sessions
            FROM analytics.scroll_events\s
            WHERE timestamp >= now() - INTERVAL %s
            GROUP BY page_url
            ORDER BY avg_scroll_percentage DESC
            LIMIT 20
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    public List<Map<String, Object>> getSessionAnalytics(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                avg(session_duration) as avg_session_duration,
                avg(page_count) as avg_pages_per_session,
                avg(total_clicks) as avg_clicks_per_session,
                countIf(is_bounce = 1) as total_bounces,
                count() as total_sessions,
                (countIf(is_bounce = 1) / count()) * 100 as bounce_rate
            FROM analytics.session_events\s
            WHERE event_type = 'session_end'
                AND timestamp >= now() - INTERVAL %s
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    public Map<String, Object> getRealtimeMetrics() throws SQLException {
        String sql = """
            SELECT\s
                (SELECT count() FROM analytics.page_view_events WHERE timestamp >= now() - INTERVAL 1 HOUR) as page_views_last_hour,
                (SELECT count() FROM analytics.click_events WHERE timestamp >= now() - INTERVAL 1 HOUR) as clicks_last_hour,
                (SELECT count() FROM analytics.scroll_events WHERE timestamp >= now() - INTERVAL 1 HOUR) as scroll_events_last_hour,
                (SELECT uniq(session_id) FROM analytics.page_view_events WHERE timestamp >= now() - INTERVAL 1 HOUR) as active_sessions,
                (SELECT uniq(user_id) FROM analytics.page_view_events WHERE timestamp >= now() - INTERVAL 1 HOUR) as active_users
           \s""";
        
        List<Map<String, Object>> results = executeQuery(sql);
        return results.isEmpty() ? new HashMap<>() : results.get(0);
    }
    
    public List<Map<String, Object>> getUserJourney(String sessionId) throws SQLException {
        String sql = """
            SELECT\s
                'page_view' as event_type,
                timestamp,
                page_url,
                page_title,
                '' as element_tag,
                '' as element_id,
                0 as scroll_depth,
                0 as session_duration
            FROM analytics.page_view_events\s
            WHERE session_id = ?
           \s
            UNION ALL
           \s
            SELECT\s
                'click' as event_type,
                timestamp,
                page_url,
                '' as page_title,
                element_tag,
                element_id,
                0 as scroll_depth,
                0 as session_duration
            FROM analytics.click_events\s
            WHERE session_id = ?
           \s
            UNION ALL
           \s
            SELECT\s
                'scroll' as event_type,
                timestamp,
                page_url,
                '' as page_title,
                '' as element_tag,
                '' as element_id,
                scroll_depth,
                0 as session_duration
            FROM analytics.scroll_events\s
            WHERE session_id = ?
           \s
            ORDER BY timestamp
           \s""";
        
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, sessionId);
            stmt.setString(3, sessionId);
            return executeQuery(stmt);
        }
    }
    
    public List<Map<String, Object>> getPerformanceMetrics() throws SQLException {
        String sql = """
            SELECT 
                page_url,
                avg(page_load_time) as avg_load_time,
                quantile(0.5)(page_load_time) as median_load_time,
                quantile(0.95)(page_load_time) as p95_load_time,
                count() as samples
            FROM analytics.page_view_events 
            WHERE page_load_time > 0
                AND timestamp >= now() - INTERVAL 24 HOUR
            GROUP BY page_url
            HAVING samples >= 10
            ORDER BY avg_load_time DESC
            """;
        
        return executeQuery(sql);
    }
    
    private String getIntervalFromTimeRange(String timeRange) {
        return switch (timeRange) {
            case "1h" -> "1 HOUR";
            case "24h" -> "24 HOUR";
            case "7d" -> "7 DAY";
            case "30d" -> "30 DAY";
            default -> "24 HOUR";
        };
    }
    
    public List<Map<String, Object>> getDashboardData(String timeRange) throws SQLException {
        String interval = getIntervalFromTimeRange(timeRange);
        
        String sql = """
            SELECT\s
                toStartOfHour(timestamp) as hour,
                count() as page_views,
                uniq(session_id) as unique_sessions,
                uniq(user_id) as unique_users
            FROM analytics.page_view_events\s
            WHERE timestamp >= now() - INTERVAL %s
            GROUP BY hour\s
            ORDER BY hour
           \s""".formatted(interval);
        
        return executeQuery(sql);
    }
    
    private List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        try (PreparedStatement stmt = clickHouseConnection.prepareStatement(sql)) {
            return executeQuery(stmt);
        }
    }
    
    private List<Map<String, Object>> executeQuery(PreparedStatement stmt) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            int columnCount = rs.getMetaData().getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        }
        
        return results;
    }
}
