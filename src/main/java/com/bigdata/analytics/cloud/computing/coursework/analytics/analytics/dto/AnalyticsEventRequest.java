package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import jakarta.validation.Valid;

import java.util.List;

public class AnalyticsEventRequest {
    
    @Valid
    private List<PageViewEvent> pageViews;
    
    @Valid
    private List<ClickEvent> clicks;
    
    @Valid
    private List<ScrollEvent> scrolls;
    
    @Valid
    private List<SessionEvent> sessions;
    
    public AnalyticsEventRequest() {}

    public List<PageViewEvent> getPageViews() { return pageViews; }
    public void setPageViews(List<PageViewEvent> pageViews) { this.pageViews = pageViews; }
    
    public List<ClickEvent> getClicks() { return clicks; }
    public void setClicks(List<ClickEvent> clicks) { this.clicks = clicks; }
    
    public List<ScrollEvent> getScrolls() { return scrolls; }
    public void setScrolls(List<ScrollEvent> scrolls) { this.scrolls = scrolls; }
    
    public List<SessionEvent> getSessions() { return sessions; }
    public void setSessions(List<SessionEvent> sessions) { this.sessions = sessions; }
    
    public int getTotalEventCount() {
        int count = 0;
        if (pageViews != null) count += pageViews.size();
        if (clicks != null) count += clicks.size();
        if (scrolls != null) count += scrolls.size();
        if (sessions != null) count += sessions.size();
        return count;
    }
    
    public boolean isEmpty() {
        return (pageViews == null || pageViews.isEmpty()) &&
               (clicks == null || clicks.isEmpty()) &&
               (scrolls == null || scrolls.isEmpty()) &&
               (sessions == null || sessions.isEmpty());
    }
}
