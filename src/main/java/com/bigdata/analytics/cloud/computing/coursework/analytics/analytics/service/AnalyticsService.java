package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto.AnalyticsEventRequest;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto.ApiResponse;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    private static final Set<String> VALID_SESSION_EVENT_TYPES = Set.of(
        "session_start", "session_end", "session_update"
    );
    
    @Autowired
    private EventEnrichmentService enrichmentService;
    
    @Autowired
    private ClickHouseService clickHouseService;
    
    public ApiResponse<Void> processEvents(AnalyticsEventRequest request, HttpServletRequest httpRequest) {
        if (request.isEmpty()) {
            return ApiResponse.error("No events provided");
        }
        
        if (request.getTotalEventCount() > 1000) {
            return ApiResponse.error("Too many events in single request (max 1000)");
        }
        
        List<ApiResponse.ValidationError> errors = new ArrayList<>();
        int processedCount = 0;
        
        try {
            if (request.getPageViews() != null && !request.getPageViews().isEmpty()) {
                List<PageViewEvent> enrichedPageViews = new ArrayList<>();
                for (int i = 0; i < request.getPageViews().size(); i++) {
                    try {
                        PageViewEvent event = request.getPageViews().get(i);
                        validatePageViewEvent(event);
                        PageViewEvent enrichedEvent = enrichmentService.enrichPageViewEvent(event, httpRequest);
                        enrichedPageViews.add(enrichedEvent);
                    } catch (Exception e) {
                        errors.add(new ApiResponse.ValidationError(i, "Page View: " + e.getMessage()));
                    }
                }
                
                if (!enrichedPageViews.isEmpty()) {
                    clickHouseService.insertPageViewEvents(enrichedPageViews);
                    processedCount += enrichedPageViews.size();
                    logger.info("Processed {} page view events", enrichedPageViews.size());
                }
            }

            if (request.getClicks() != null && !request.getClicks().isEmpty()) {
                List<ClickEvent> enrichedClicks = new ArrayList<>();
                for (int i = 0; i < request.getClicks().size(); i++) {
                    try {
                        ClickEvent event = request.getClicks().get(i);
                        validateClickEvent(event);
                        ClickEvent enrichedEvent = enrichmentService.enrichClickEvent(event, httpRequest);
                        enrichedClicks.add(enrichedEvent);
                    } catch (Exception e) {
                        errors.add(new ApiResponse.ValidationError(i, "Click: " + e.getMessage()));
                    }
                }
                
                if (!enrichedClicks.isEmpty()) {
                    clickHouseService.insertClickEvents(enrichedClicks);

                    processedCount += enrichedClicks.size();
                    logger.info("Processed {} click events", enrichedClicks.size());
                }
            }

            if (request.getScrolls() != null && !request.getScrolls().isEmpty()) {
                List<ScrollEvent> enrichedScrolls = new ArrayList<>();
                for (int i = 0; i < request.getScrolls().size(); i++) {
                    try {
                        ScrollEvent event = request.getScrolls().get(i);
                        validateScrollEvent(event);
                        ScrollEvent enrichedEvent = enrichmentService.enrichScrollEvent(event, httpRequest);
                        enrichedScrolls.add(enrichedEvent);
                    } catch (Exception e) {
                        errors.add(new ApiResponse.ValidationError(i, "Scroll: " + e.getMessage()));
                    }
                }
                
                if (!enrichedScrolls.isEmpty()) {
                    clickHouseService.insertScrollEvents(enrichedScrolls);
                    processedCount += enrichedScrolls.size();
                    logger.info("Processed {} scroll events", enrichedScrolls.size());
                }
            }

            if (request.getSessions() != null && !request.getSessions().isEmpty()) {
                List<SessionEvent> enrichedSessions = new ArrayList<>();
                for (int i = 0; i < request.getSessions().size(); i++) {
                    try {
                        SessionEvent event = request.getSessions().get(i);
                        validateSessionEvent(event);
                        SessionEvent enrichedEvent = enrichmentService.enrichSessionEvent(event, httpRequest);
                        enrichedSessions.add(enrichedEvent);
                    } catch (Exception e) {
                        errors.add(new ApiResponse.ValidationError(i, "Session: " + e.getMessage()));
                    }
                }
                
                if (!enrichedSessions.isEmpty()) {
                    clickHouseService.insertSessionEvents(enrichedSessions);
                    processedCount += enrichedSessions.size();
                    logger.info("Processed {} session events", enrichedSessions.size());
                }
            }
            
            if (processedCount == 0) {
                ApiResponse<Void> response = ApiResponse.error("No valid events to process");
                response.setErrors(errors);
                return response;
            }
            
            ApiResponse<Void> response = ApiResponse.success();
            response.setProcessed(processedCount);
            if (!errors.isEmpty()) {
                response.setErrors(errors);
            }
            return response;
            
        } catch (SQLException e) {
            logger.error("Failed to insert events to ClickHouse", e);
            return ApiResponse.error("Failed to process events: " + e.getMessage());
        }
    }
    
    private void validatePageViewEvent(PageViewEvent event) {
        List<String> missingFields = new ArrayList<>();
        
        if (event.getSessionId() == null || event.getSessionId().trim().isEmpty()) {
            missingFields.add("session_id");
        }
        if (event.getPageUrl() == null || event.getPageUrl().trim().isEmpty()) {
            missingFields.add("page_url");
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + String.join(", ", missingFields));
        }
    }
    
    private void validateClickEvent(ClickEvent event) {
        List<String> missingFields = new ArrayList<>();
        
        if (event.getSessionId() == null || event.getSessionId().trim().isEmpty()) {
            missingFields.add("session_id");
        }
        if (event.getPageUrl() == null || event.getPageUrl().trim().isEmpty()) {
            missingFields.add("page_url");
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + String.join(", ", missingFields));
        }
    }
    
    private void validateScrollEvent(ScrollEvent event) {
        List<String> missingFields = new ArrayList<>();
        
        if (event.getSessionId() == null || event.getSessionId().trim().isEmpty()) {
            missingFields.add("session_id");
        }
        if (event.getPageUrl() == null || event.getPageUrl().trim().isEmpty()) {
            missingFields.add("page_url");
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + String.join(", ", missingFields));
        }
    }
    
    private void validateSessionEvent(SessionEvent event) {
        List<String> missingFields = new ArrayList<>();
        
        if (event.getSessionId() == null || event.getSessionId().trim().isEmpty()) {
            missingFields.add("session_id");
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            missingFields.add("event_type");
        }
        
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + String.join(", ", missingFields));
        }
        
        if (!VALID_SESSION_EVENT_TYPES.contains(event.getEventType())) {
            throw new IllegalArgumentException("Invalid session event type: " + event.getEventType());
        }
    }
}
