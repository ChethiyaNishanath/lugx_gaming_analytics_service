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
        if (!exportEnabled || pageViewData == null || pageViewData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(pageViewData, "page_view_events", "realtime");
            logger.debug("Successfully exported {} page view events to S3", pageViewData.size());
        } catch (Exception e) {
            logger.error("Failed to export page view events to S3", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of click events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportClickEventsAsync(List<Map<String, Object>> clickData) {
        if (!exportEnabled || clickData == null || clickData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(clickData, "click_events", "realtime");
            logger.debug("Successfully exported {} click events to S3", clickData.size());
        } catch (Exception e) {
            logger.error("Failed to export click events to S3", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of scroll events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportScrollEventsAsync(List<Map<String, Object>> scrollData) {
        if (!exportEnabled || scrollData == null || scrollData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(scrollData, "scroll_events", "realtime");
            logger.debug("Successfully exported {} scroll events to S3", scrollData.size());
        } catch (Exception e) {
            logger.error("Failed to export scroll events to S3", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Async export of session events to S3 immediately after data ingestion
     */
    @Async("s3ExportExecutor")
    public CompletableFuture<Void> exportSessionEventsAsync(List<Map<String, Object>> sessionData) {
        if (!exportEnabled || sessionData == null || sessionData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        try {
            exportToS3AsCsv(sessionData, "session_events", "realtime");
            logger.debug("Successfully exported {} session events to S3", sessionData.size());
        } catch (Exception e) {
            logger.error("Failed to export session events to S3", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Generic method to export data to S3 as CSV for QuickSight
     */
    private void exportToS3AsCsv(List<Map<String, Object>> data, String dataType, String timeRange) {
        try {
            if (data.isEmpty()) {
                logger.warn("No data to export for {}", dataType);
                return;
            }
            
            // Create CSV content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
            
            // Write header
            Map<String, Object> firstRow = data.get(0);
            String[] headers = firstRow.keySet().toArray(new String[0]);
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
            
            csvWriter.close();
            byte[] csvBytes = outputStream.toByteArray();
            
            // Generate S3 key
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/HH"));
            String fileName = String.format("%s_%s_%s.csv", 
                dataType, 
                timeRange.replaceAll("[^a-zA-Z0-9]", "_"), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            );
            String s3Key = quicksightPrefix + timestamp + "/" + fileName;
            
            // Upload to S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("text/csv")
                .metadata(Map.of(
                    "data-type", dataType,
                    "time-range", timeRange,
                    "export-timestamp", LocalDateTime.now().toString(),
                    "record-count", String.valueOf(data.size())
                ))
                .build();
            
            s3Client.putObject(putRequest, RequestBody.fromBytes(csvBytes));
            
            String s3Url = String.format("s3://%s/%s", bucketName, s3Key);
            logger.info("Successfully exported {} records of {} to S3: {}", data.size(), dataType, s3Url);
            
        } catch (Exception e) {
            logger.error("Failed to export {} to S3", dataType, e);
            throw new RuntimeException("Failed to export data to S3", e);
        }
    }
}
