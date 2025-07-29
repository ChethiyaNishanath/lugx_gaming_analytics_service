package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.BaseAnalyticsEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class EventEnrichmentService {
    
    public PageViewEvent enrichPageViewEvent(PageViewEvent event, HttpServletRequest request) {
        enrichBaseEvent(event, request);
        
        // Ensure page view specific fields are not null
        if (event.getPageTitle() == null) event.setPageTitle("");
        if (event.getPageLoadTime() == null) event.setPageLoadTime(0);
        if (event.getTimeOnPage() == null) event.setTimeOnPage(0);
        if (event.getIsBounce() == null) event.setIsBounce(false);
        if (event.getEntryPage() == null) event.setEntryPage(false);
        if (event.getExitPage() == null) event.setExitPage(false);
        
        return event;
    }
    
    public ClickEvent enrichClickEvent(ClickEvent event, HttpServletRequest request) {
        enrichBaseEvent(event, request);
        
        // Ensure click specific fields are not null
        if (event.getClickX() == null) event.setClickX(0);
        if (event.getClickY() == null) event.setClickY(0);
        if (event.getElementId() == null) event.setElementId("");
        if (event.getElementClass() == null) event.setElementClass("");
        if (event.getElementTag() == null) event.setElementTag("");
        if (event.getElementText() == null) event.setElementText("");
        if (event.getClickType() == null) event.setClickType("left");
        if (event.getIsDoubleClick() == null) event.setIsDoubleClick(false);
        
        return event;
    }
    
    public ScrollEvent enrichScrollEvent(ScrollEvent event, HttpServletRequest request) {
        enrichBaseEvent(event, request);
        
        // Ensure scroll specific fields are not null
        if (event.getScrollDepth() == null) event.setScrollDepth(0);
        if (event.getMaxScrollDepth() == null) event.setMaxScrollDepth(0);
        if (event.getScrollPercentage() == null) event.setScrollPercentage(0.0);
        if (event.getScrollDirection() == null) event.setScrollDirection("down");
        if (event.getScrollSpeed() == null) event.setScrollSpeed(0);
        if (event.getTimeToScroll() == null) event.setTimeToScroll(0);
        
        return event;
    }
    
    public SessionEvent enrichSessionEvent(SessionEvent event, HttpServletRequest request) {
        enrichBaseEvent(event, request);
        
        // Ensure session specific fields are not null
        if (event.getEventType() == null) event.setEventType("session_start");
        if (event.getSessionDuration() == null) event.setSessionDuration(0);
        if (event.getPageCount() == null) event.setPageCount(1);
        if (event.getTotalClicks() == null) event.setTotalClicks(0);
        if (event.getTotalScrollDepth() == null) event.setTotalScrollDepth(0);
        if (event.getIsBounce() == null) event.setIsBounce(false);
        if (event.getEntryPage() == null) event.setEntryPage("");
        if (event.getExitPage() == null) event.setExitPage("");
        
        return event;
    }
    
    private void enrichBaseEvent(BaseAnalyticsEvent event, HttpServletRequest request) {
        // Generate event ID if not present
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        
        // Set timestamp if not present
        if (event.getTimestamp() == null || event.getTimestamp().isEmpty()) {
            event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        // Set server timestamp
        event.setServerTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Get user agent info
        String userAgentString = event.getUserAgent();
        if (userAgentString == null || userAgentString.isEmpty()) {
            userAgentString = request.getHeader("User-Agent");
            event.setUserAgent(userAgentString);
        }
        
        if (userAgentString != null) {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            
            // Browser info
            if (event.getBrowser() == null || event.getBrowser().isEmpty()) {
                event.setBrowser(userAgent.getBrowser().getName());
            }
            if (event.getBrowserVersion() == null || event.getBrowserVersion().isEmpty()) {
                event.setBrowserVersion(userAgent.getBrowserVersion().getVersion());
            }
            
            // OS info
            if (event.getOs() == null || event.getOs().isEmpty()) {
                event.setOs(userAgent.getOperatingSystem().getName());
            }
            
            // Device type
            if (event.getDeviceType() == null || event.getDeviceType().isEmpty()) {
                event.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
            }
        }
        
        // Geographic data (simplified - you would use a proper GeoIP service)
        String clientIP = getClientIP(request);
        if (event.getCountry() == null || event.getCountry().isEmpty()) {
            // This is a placeholder - in real implementation, use MaxMind GeoIP2
            event.setCountry("Unknown");
        }
        if (event.getCity() == null || event.getCity().isEmpty()) {
            event.setCity("Unknown");
        }
        
        // Ensure referrer is not null
        if (event.getReferrer() == null) {
            event.setReferrer(request.getHeader("Referer") != null ? request.getHeader("Referer") : "");
        }
        
        // Ensure all string fields are not null
        ensureBaseFieldsNotNull(event);
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
    
    private void ensureBaseFieldsNotNull(BaseAnalyticsEvent event) {
        if (event.getUserAgent() == null) event.setUserAgent("");
        if (event.getBrowser() == null) event.setBrowser("Unknown");
        if (event.getBrowserVersion() == null) event.setBrowserVersion("");
        if (event.getOs() == null) event.setOs("Unknown");
        if (event.getOsVersion() == null) event.setOsVersion("");
        if (event.getDeviceType() == null) event.setDeviceType("Unknown");
        if (event.getCountry() == null) event.setCountry("Unknown");
        if (event.getCity() == null) event.setCity("Unknown");
        if (event.getScreenWidth() == null) event.setScreenWidth(0);
        if (event.getScreenHeight() == null) event.setScreenHeight(0);
        if (event.getViewportWidth() == null) event.setViewportWidth(0);
        if (event.getViewportHeight() == null) event.setViewportHeight(0);
    }
}
