package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageViewEvent extends BaseAnalyticsEvent {
    
    @JsonProperty("page_title")
    private String pageTitle;
    
    @JsonProperty("page_load_time")
    private Integer pageLoadTime = 0;
    
    @JsonProperty("time_on_page")
    private Integer timeOnPage = 0;
    
    @JsonProperty("is_bounce")
    private Boolean isBounce = false;
    
    @JsonProperty("entry_page")
    private Boolean entryPage = false;
    
    @JsonProperty("exit_page")
    private Boolean exitPage = false;
    
    // Constructors
    public PageViewEvent() {
        super();
    }
    
    // Getters and Setters
    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
    
    public Integer getPageLoadTime() { return pageLoadTime; }
    public void setPageLoadTime(Integer pageLoadTime) { this.pageLoadTime = pageLoadTime; }
    
    public Integer getTimeOnPage() { return timeOnPage; }
    public void setTimeOnPage(Integer timeOnPage) { this.timeOnPage = timeOnPage; }
    
    public Boolean getIsBounce() { return isBounce; }
    public void setIsBounce(Boolean isBounce) { this.isBounce = isBounce; }
    
    public Boolean getEntryPage() { return entryPage; }
    public void setEntryPage(Boolean entryPage) { this.entryPage = entryPage; }
    
    public Boolean getExitPage() { return exitPage; }
    public void setExitPage(Boolean exitPage) { this.exitPage = exitPage; }
}
