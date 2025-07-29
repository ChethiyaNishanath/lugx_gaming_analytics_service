package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionEvent extends BaseAnalyticsEvent {
    
    @JsonProperty("event_type")
    private String eventType; // session_start, session_end, session_update
    
    @JsonProperty("session_duration")
    private Integer sessionDuration = 0;
    
    @JsonProperty("page_count")
    private Integer pageCount = 1;
    
    @JsonProperty("total_clicks")
    private Integer totalClicks = 0;
    
    @JsonProperty("total_scroll_depth")
    private Integer totalScrollDepth = 0;
    
    @JsonProperty("bounce_rate")
    private Boolean isBounce = false;
    
    @JsonProperty("entry_page")
    private String entryPage;
    
    @JsonProperty("exit_page")
    private String exitPage;
    
    // Constructors
    public SessionEvent() {
        super();
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Integer getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Integer sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    
    public Integer getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Integer totalClicks) { this.totalClicks = totalClicks; }
    
    public Integer getTotalScrollDepth() { return totalScrollDepth; }
    public void setTotalScrollDepth(Integer totalScrollDepth) { this.totalScrollDepth = totalScrollDepth; }
    
    public Boolean getIsBounce() { return isBounce; }
    public void setIsBounce(Boolean isBounce) { this.isBounce = isBounce; }
    
    public String getEntryPage() { return entryPage; }
    public void setEntryPage(String entryPage) { this.entryPage = entryPage; }
    
    public String getExitPage() { return exitPage; }
    public void setExitPage(String exitPage) { this.exitPage = exitPage; }
}
