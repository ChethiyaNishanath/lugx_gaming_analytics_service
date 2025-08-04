package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataClient;
import software.amazon.awssdk.services.redshiftdata.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "aws.redshift.enabled", havingValue = "true", matchIfMissing = false)
public class RedshiftService {

    private static final Logger logger = LoggerFactory.getLogger(RedshiftService.class);

    @Autowired
    @Qualifier("redshiftConnection")
    private Connection redshiftConnection;

    @Autowired
    private RedshiftDataClient redshiftDataClient;

    @Value("${aws.redshift.cluster.id}")
    private String clusterId;

    @Value("${aws.redshift.database}")
    private String database;

    @Value("${aws.redshift.username}")
    private String username;

    @Value("${aws.redshift.schema:public}")
    private String schema;

    @Value("${aws.redshift.async.enabled:true}")
    private boolean asyncEnabled;

    @Value("${aws.redshift.batch.size:1000}")
    private int batchSize;

    @Value("${aws.redshift.query.timeout:300}")
    private int queryTimeout;

    /**
     * Async insert of page view events to Redshift
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> insertPageViewEventsAsync(List<PageViewEvent> events) {
        if (!asyncEnabled || events == null || events.isEmpty()) {
            logger.debug("Redshift async insert disabled or no page view events to insert");
            return CompletableFuture.completedFuture(null);
        }

        try {
            insertPageViewEvents(events);
            logger.info("Successfully inserted {} page view events to Redshift asynchronously", events.size());
        } catch (Exception e) {
            logger.error("Failed to insert page view events to Redshift asynchronously", e);
            // Don't rethrow to avoid breaking main flow
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sync insert of page view events to Redshift
     */
    public void insertPageViewEvents(List<PageViewEvent> events) throws SQLException {
        if (events == null || events.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO %s.page_view_events (
                session_id, user_id, page_url, page_title, referrer, load_time,
                timestamp, user_agent, ip_address, device_type, browser, os, country, city
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(schema);

        try (PreparedStatement stmt = redshiftConnection.prepareStatement(sql)) {
            int batchCount = 0;

            for (PageViewEvent event : events) {
                stmt.setString(1, event.getSessionId());
                stmt.setString(2, event.getUserId());
                stmt.setString(3, event.getPageUrl());
                stmt.setString(4, event.getPageTitle());
                stmt.setString(5, event.getReferrer());
                stmt.setObject(6, event.getLoadTime());
                stmt.setString(7, event.getTimestamp());
                stmt.setString(8, event.getUserAgent());
                stmt.setString(9, event.getIpAddress());
                stmt.setString(10, event.getDeviceType());
                stmt.setString(11, event.getBrowser());
                stmt.setString(12, event.getOs());
                stmt.setString(13, event.getCountry());
                stmt.setString(14, event.getCity());

                stmt.addBatch();
                batchCount++;

                // Execute batch when it reaches the configured size
                if (batchCount >= batchSize) {
                    stmt.executeBatch();
                    redshiftConnection.commit();
                    logger.debug("Executed batch of {} page view events", batchCount);
                    batchCount = 0;
                }
            }

            // Execute any remaining items in the batch
            if (batchCount > 0) {
                stmt.executeBatch();
                redshiftConnection.commit();
                logger.debug("Executed final batch of {} page view events", batchCount);
            }

            logger.info("Successfully inserted {} page view events into Redshift", events.size());
        }
    }

    /**
     * Async insert of click events to Redshift
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> insertClickEventsAsync(List<ClickEvent> events) {
        if (!asyncEnabled || events == null || events.isEmpty()) {
            logger.debug("Redshift async insert disabled or no click events to insert");
            return CompletableFuture.completedFuture(null);
        }

        try {
            insertClickEvents(events);
            logger.info("Successfully inserted {} click events to Redshift asynchronously", events.size());
        } catch (Exception e) {
            logger.error("Failed to insert click events to Redshift asynchronously", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sync insert of click events to Redshift
     */
    public void insertClickEvents(List<ClickEvent> events) throws SQLException {
        if (events == null || events.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO %s.click_events (
                session_id, user_id, element_id, element_text,
                page_url, click_x, click_y, timestamp, user_agent, ip_address, device_type,
                browser, os, country, city
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(schema);

        try (PreparedStatement stmt = redshiftConnection.prepareStatement(sql)) {
            int batchCount = 0;

            for (ClickEvent event : events) {
                stmt.setString(1, event.getSessionId());
                stmt.setString(2, event.getUserId());
                stmt.setString(3, event.getElementId());
                stmt.setString(4, event.getElementText());
                stmt.setString(5, event.getPageUrl());
                stmt.setObject(6, event.getClickX());
                stmt.setObject(7, event.getClickY());
                stmt.setString(8, event.getTimestamp());
                stmt.setString(9, event.getUserAgent());
                stmt.setString(10, event.getIpAddress());
                stmt.setString(11, event.getDeviceType());
                stmt.setString(12, event.getBrowser());
                stmt.setString(13, event.getOs());
                stmt.setString(14, event.getCountry());
                stmt.setString(15, event.getCity());

                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    stmt.executeBatch();
                    redshiftConnection.commit();
                    logger.debug("Executed batch of {} click events", batchCount);
                    batchCount = 0;
                }
            }

            if (batchCount > 0) {
                stmt.executeBatch();
                redshiftConnection.commit();
                logger.debug("Executed final batch of {} click events", batchCount);
            }

            logger.info("Successfully inserted {} click events into Redshift", events.size());
        }
    }

    /**
     * Async insert of scroll events to Redshift
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> insertScrollEventsAsync(List<ScrollEvent> events) {
        if (!asyncEnabled || events == null || events.isEmpty()) {
            logger.debug("Redshift async insert disabled or no scroll events to insert");
            return CompletableFuture.completedFuture(null);
        }

        try {
            insertScrollEvents(events);
            logger.info("Successfully inserted {} scroll events to Redshift asynchronously", events.size());
        } catch (Exception e) {
            logger.error("Failed to insert scroll events to Redshift asynchronously", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sync insert of scroll events to Redshift
     */
    public void insertScrollEvents(List<ScrollEvent> events) throws SQLException {
        if (events == null || events.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO %s.scroll_events (
                session_id, user_id, page_url, scroll_depth, scroll_percentage,
                timestamp, user_agent, ip_address, device_type, browser, os, country, city
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(schema);

        try (PreparedStatement stmt = redshiftConnection.prepareStatement(sql)) {
            int batchCount = 0;

            for (ScrollEvent event : events) {
                stmt.setString(1, event.getSessionId());
                stmt.setString(2, event.getUserId());
                stmt.setString(3, event.getPageUrl());
                stmt.setObject(4, event.getScrollDepth());
                stmt.setObject(5, event.getScrollPercentage());
                stmt.setString(6, event.getTimestamp());
                stmt.setString(7, event.getUserAgent());
                stmt.setString(8, event.getIpAddress());
                stmt.setString(9, event.getDeviceType());
                stmt.setString(10, event.getBrowser());
                stmt.setString(11, event.getOs());
                stmt.setString(12, event.getCountry());
                stmt.setString(13, event.getCity());

                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    stmt.executeBatch();
                    redshiftConnection.commit();
                    logger.debug("Executed batch of {} scroll events", batchCount);
                    batchCount = 0;
                }
            }

            if (batchCount > 0) {
                stmt.executeBatch();
                redshiftConnection.commit();
                logger.debug("Executed final batch of {} scroll events", batchCount);
            }

            logger.info("Successfully inserted {} scroll events into Redshift", events.size());
        }
    }

    /**
     * Async insert of session events to Redshift
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> insertSessionEventsAsync(List<SessionEvent> events) {
        if (!asyncEnabled || events == null || events.isEmpty()) {
            logger.debug("Redshift async insert disabled or no session events to insert");
            return CompletableFuture.completedFuture(null);
        }

        try {
            insertSessionEvents(events);
            logger.info("Successfully inserted {} session events to Redshift asynchronously", events.size());
        } catch (Exception e) {
            logger.error("Failed to insert session events to Redshift asynchronously", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Sync insert of session events to Redshift
     */
    public void insertSessionEvents(List<SessionEvent> events) throws SQLException {
        if (events == null || events.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO %s.session_events (
                session_id, user_id, event_type, page_count,
                timestamp, user_agent, ip_address, device_type, browser, os, country, city
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(schema);

        try (PreparedStatement stmt = redshiftConnection.prepareStatement(sql)) {
            int batchCount = 0;

            for (SessionEvent event : events) {
                stmt.setString(1, event.getSessionId());
                stmt.setString(2, event.getUserId());
                stmt.setString(3, event.getEventType());
                stmt.setObject(4, event.getPageCount());
                stmt.setString(5, event.getTimestamp());
                stmt.setString(6, event.getUserAgent());
                stmt.setString(7, event.getIpAddress());
                stmt.setString(8, event.getDeviceType());
                stmt.setString(9, event.getBrowser());
                stmt.setString(10, event.getOs());
                stmt.setString(11, event.getCountry());
                stmt.setString(12, event.getCity());

                stmt.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    stmt.executeBatch();
                    redshiftConnection.commit();
                    logger.debug("Executed batch of {} session events", batchCount);
                    batchCount = 0;
                }
            }

            if (batchCount > 0) {
                stmt.executeBatch();
                redshiftConnection.commit();
                logger.debug("Executed final batch of {} session events", batchCount);
            }

            logger.info("Successfully inserted {} session events into Redshift", events.size());
        }
    }

    /**
     * Health check for Redshift connection
     */
    public boolean isHealthy() {
        try (PreparedStatement stmt = redshiftConnection.prepareStatement("SELECT 1")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            logger.error("Redshift health check failed", e);
            return false;
        }
    }

    /**
     * Execute DDL using Redshift Data API for table creation
     */
    public void executeDDL(String sql) {
        try {
            ExecuteStatementRequest request = ExecuteStatementRequest.builder()
                    .clusterIdentifier(clusterId)
                    .database(database)
                    .dbUser(username)
                    .sql(sql)
                    .build();

            ExecuteStatementResponse response = redshiftDataClient.executeStatement(request);
            logger.info("DDL executed successfully. Query ID: {}", response.id());

            // Wait for completion
            waitForQueryCompletion(response.id());

        } catch (Exception e) {
            logger.error("Failed to execute DDL: {}", sql, e);
            throw new RuntimeException("Failed to execute DDL", e);
        }
    }

    /**
     * Wait for query completion using Redshift Data API
     */
    private void waitForQueryCompletion(String queryId) {
        try {
            int maxAttempts = 60; // 5 minutes max wait
            int attempt = 0;

            while (attempt < maxAttempts) {
                DescribeStatementRequest describeRequest = DescribeStatementRequest.builder()
                        .id(queryId)
                        .build();

                DescribeStatementResponse describeResponse = redshiftDataClient.describeStatement(describeRequest);
                StatusString status = describeResponse.status();

                if (status == StatusString.FINISHED) {
                    logger.info("Query {} completed successfully", queryId);
                    return;
                } else if (status == StatusString.FAILED || status == StatusString.ABORTED) {
                    logger.error("Query {} failed with status: {}", queryId, status);
                    throw new RuntimeException("Query failed with status: " + status);
                }

                Thread.sleep(5000); // Wait 5 seconds before checking again
                attempt++;
            }

            throw new RuntimeException("Query timed out after " + maxAttempts * 5 + " seconds");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Query wait interrupted", e);
        }
    }

    /**
     * Get current database name
     */
    public String getCurrentDatabase() {
        try (PreparedStatement stmt = redshiftConnection.prepareStatement("SELECT current_database()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String currentDb = rs.getString(1);
                logger.info("Currently connected to Redshift database: {}", currentDb);
                return currentDb;
            }
            return "unknown";
        } catch (SQLException e) {
            logger.error("Failed to get current Redshift database", e);
            return "error";
        }
    }
}
