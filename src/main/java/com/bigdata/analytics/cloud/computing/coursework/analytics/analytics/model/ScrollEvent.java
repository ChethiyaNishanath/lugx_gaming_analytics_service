package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScrollEvent extends BaseAnalyticsEvent {
    
    @JsonProperty("scroll_depth")
    private Integer scrollDepth = 0;
    
    @JsonProperty("max_scroll_depth")
    private Integer maxScrollDepth = 0;
    
    @JsonProperty("scroll_percentage")
    private Double scrollPercentage = 0.0;
    
    @JsonProperty("scroll_direction")
    private String scrollDirection = "down"; // up, down
    
    @JsonProperty("scroll_speed")
    private Integer scrollSpeed = 0; // pixels per second
    
    @JsonProperty("time_to_scroll")
    private Integer timeToScroll = 0; // time since page load
    
    // Constructors
    public ScrollEvent() {
        super();
    }
    
    // Getters and Setters
    public Integer getScrollDepth() { return scrollDepth; }
    public void setScrollDepth(Integer scrollDepth) { this.scrollDepth = scrollDepth; }
    
    public Integer getMaxScrollDepth() { return maxScrollDepth; }
    public void setMaxScrollDepth(Integer maxScrollDepth) { this.maxScrollDepth = maxScrollDepth; }
    
    public Double getScrollPercentage() { return scrollPercentage; }
    public void setScrollPercentage(Double scrollPercentage) { this.scrollPercentage = scrollPercentage; }
    
    public String getScrollDirection() { return scrollDirection; }
    public void setScrollDirection(String scrollDirection) { this.scrollDirection = scrollDirection; }
    
    public Integer getScrollSpeed() { return scrollSpeed; }
    public void setScrollSpeed(Integer scrollSpeed) { this.scrollSpeed = scrollSpeed; }
    
    public Integer getTimeToScroll() { return timeToScroll; }
    public void setTimeToScroll(Integer timeToScroll) { this.timeToScroll = timeToScroll; }
}
