package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class AnalyticsEvent {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @NotBlank(message = "Session ID is required")
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @NotBlank(message = "Event type is required")
    @Pattern(regexp = "page_view|click|scroll|session_start|session_end|page_exit|custom|session_update", 
             message = "Invalid event type")
    @JsonProperty("event_type")
    private String eventType;
    
    @NotBlank(message = "Page URL is required")
    @JsonProperty("page_url")
    private String pageUrl;
    
    @JsonProperty("page_title")
    private String pageTitle;
    
    private String timestamp;
    
    @JsonProperty("server_timestamp")
    private String serverTimestamp;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    private String country;
    private String city;
    private String browser;
    
    @JsonProperty("browser_version")
    private String browserVersion;
    
    private String os;
    
    @JsonProperty("os_version")
    private String osVersion;
    
    @JsonProperty("device_type")
    private String deviceType;
    
    @JsonProperty("page_load_time")
    private Integer pageLoadTime = 0;
    
    @JsonProperty("scroll_depth")
    private Integer scrollDepth = 0;
    
    @JsonProperty("max_scroll_depth")
    private Integer maxScrollDepth = 0;
    
    @JsonProperty("session_duration")
    private Integer sessionDuration = 0;
    
    @JsonProperty("page_count")
    private Integer pageCount = 1;
    
    @JsonProperty("click_x")
    private Integer clickX = 0;
    
    @JsonProperty("click_y")
    private Integer clickY = 0;
    
    @JsonProperty("screen_width")
    private Integer screenWidth = 0;
    
    @JsonProperty("screen_height")
    private Integer screenHeight = 0;
    
    @JsonProperty("viewport_width")
    private Integer viewportWidth = 0;
    
    @JsonProperty("viewport_height")
    private Integer viewportHeight = 0;
    
    @JsonProperty("element_id")
    private String elementId = "";
    
    @JsonProperty("element_class")
    private String elementClass = "";
    
    @JsonProperty("element_tag")
    private String elementTag = "";
    
    private String referrer = "";
    
    // Constructors
    public AnalyticsEvent() {}
    
    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    
    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(String serverTimestamp) { this.serverTimestamp = serverTimestamp; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    
    public String getBrowserVersion() { return browserVersion; }
    public void setBrowserVersion(String browserVersion) { this.browserVersion = browserVersion; }
    
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public Integer getPageLoadTime() { return pageLoadTime; }
    public void setPageLoadTime(Integer pageLoadTime) { this.pageLoadTime = pageLoadTime; }
    
    public Integer getScrollDepth() { return scrollDepth; }
    public void setScrollDepth(Integer scrollDepth) { this.scrollDepth = scrollDepth; }
    
    public Integer getMaxScrollDepth() { return maxScrollDepth; }
    public void setMaxScrollDepth(Integer maxScrollDepth) { this.maxScrollDepth = maxScrollDepth; }
    
    public Integer getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Integer sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    
    public Integer getClickX() { return clickX; }
    public void setClickX(Integer clickX) { this.clickX = clickX; }
    
    public Integer getClickY() { return clickY; }
    public void setClickY(Integer clickY) { this.clickY = clickY; }
    
    public Integer getScreenWidth() { return screenWidth; }
    public void setScreenWidth(Integer screenWidth) { this.screenWidth = screenWidth; }
    
    public Integer getScreenHeight() { return screenHeight; }
    public void setScreenHeight(Integer screenHeight) { this.screenHeight = screenHeight; }
    
    public Integer getViewportWidth() { return viewportWidth; }
    public void setViewportWidth(Integer viewportWidth) { this.viewportWidth = viewportWidth; }
    
    public Integer getViewportHeight() { return viewportHeight; }
    public void setViewportHeight(Integer viewportHeight) { this.viewportHeight = viewportHeight; }
    
    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }
    
    public String getElementClass() { return elementClass; }
    public void setElementClass(String elementClass) { this.elementClass = elementClass; }
    
    public String getElementTag() { return elementTag; }
    public void setElementTag(String elementTag) { this.elementTag = elementTag; }
    
    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
}
