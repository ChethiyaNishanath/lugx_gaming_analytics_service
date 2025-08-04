package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class S3ExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ExportService.class);
    
    @Autowired
    private S3Client s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.quicksight-prefix:quicksight/analytics/}")
    private String quicksightPrefix;
    
    @Value("${aws.s3.export.enabled:true}")
    private boolean exportEnabled;

    /**
     * Async export of page view events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportPageViewEventsAsync(List<Map<String, Object>> pageViewData) {
        if (!exportEnabled) {
            logger.debug("S3 export is disabled, skipping page view events export");
            return CompletableFuture.completedFuture(null);
        }
        
        if (pageViewData == null || pageViewData.isEmpty()) {
            logger.debug("No page view data to export to S3");
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(pageViewData, "page_view_events", "realtime");
            logger.info("Successfully exported {} page view events to S3 asynchronously", pageViewData.size());
        } catch (Exception e) {
            logger.error("Failed to export page view events to S3", e);
            // Don't rethrow exception to avoid breaking the main flow
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of click events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportClickEventsAsync(List<Map<String, Object>> clickData) {
        if (!exportEnabled) {
            logger.debug("S3 export is disabled, skipping click events export");
            return CompletableFuture.completedFuture(null);
        }
        
        if (clickData == null || clickData.isEmpty()) {
            logger.debug("No click data to export to S3");
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(clickData, "click_events", "realtime");
            logger.info("Successfully exported {} click events to S3 asynchronously", clickData.size());
        } catch (Exception e) {
            logger.error("Failed to export click events to S3", e);
            // Don't rethrow exception to avoid breaking the main flow
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of scroll events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportScrollEventsAsync(List<Map<String, Object>> scrollData) {
        if (!exportEnabled) {
            logger.debug("S3 export is disabled, skipping scroll events export");
            return CompletableFuture.completedFuture(null);
        }
        
        if (scrollData == null || scrollData.isEmpty()) {
            logger.debug("No scroll data to export to S3");
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(scrollData, "scroll_events", "realtime");
            logger.info("Successfully exported {} scroll events to S3 asynchronously", scrollData.size());
        } catch (Exception e) {
            logger.error("Failed to export scroll events to S3", e);
            // Don't rethrow exception to avoid breaking the main flow
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of session events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportSessionEventsAsync(List<Map<String, Object>> sessionData) {
        if (!exportEnabled) {
            logger.debug("S3 export is disabled, skipping session events export");
            return CompletableFuture.completedFuture(null);
        }
        
        if (sessionData == null || sessionData.isEmpty()) {
            logger.debug("No session data to export to S3");
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(sessionData, "session_events", "realtime");
            logger.info("Successfully exported {} session events to S3 asynchronously", sessionData.size());
        } catch (Exception e) {
            logger.error("Failed to export session events to S3", e);
            // Don't rethrow exception to avoid breaking the main flow
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Generic method to export data to S3 as CSV for QuickSight
     */
    private void exportToS3AsCsv(List<Map<String, Object>> data, String dataType, String timeRange) {
        String s3Key = null;
        try {
            if (data.isEmpty()) {
                logger.warn("No data to export for {}", dataType);
                return;
            }
            
            // Create CSV content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream))) {
                
                // Write header
                Map<String, Object> firstRow = data.get(0);
                String[] headers = firstRow.keySet().stream().sorted().toArray(String[]::new);
                csvWriter.writeNext(headers);
                
                // Write data rows
                for (Map<String, Object> row : data) {
                    String[] values = new String[headers.length];
                    for (int i = 0; i < headers.length; i++) {
                        Object value = row.get(headers[i]);
                        values[i] = value != null ? value.toString() : "";
                    }
                    csvWriter.writeNext(values);
                }
            }
            
            byte[] csvBytes = outputStream.toByteArray();
            
            // Generate S3 key with better structure
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd/HH"));
            String fileName = String.format("%s_%s_%s_%d.csv", 
                dataType, 
                timeRange.replaceAll("[^a-zA-Z0-9]", "_"), 
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")),
                System.nanoTime() % 10000 // Add unique identifier to avoid collisions
            );
            s3Key = quicksightPrefix + timestamp + "/" + fileName;
            
            // Upload to S3 with retry logic
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("text/csv")
                .metadata(Map.of(
                    "data-type", dataType,
                    "time-range", timeRange,
                    "export-timestamp", now.toString(),
                    "record-count", String.valueOf(data.size()),
                    "file-size", String.valueOf(csvBytes.length)
                ))
                .build();
            
            s3Client.putObject(putRequest, RequestBody.fromBytes(csvBytes));
            
            String s3Url = String.format("s3://%s/%s", bucketName, s3Key);
            logger.info("Successfully exported {} records of {} ({} bytes) to S3: {}", 
                data.size(), dataType, csvBytes.length, s3Url);
            
        } catch (Exception e) {
            logger.error("Failed to export {} to S3 (key: {})", dataType, s3Key, e);
            // Don't rethrow exception to avoid breaking the main analytics flow
        }
    }
}
