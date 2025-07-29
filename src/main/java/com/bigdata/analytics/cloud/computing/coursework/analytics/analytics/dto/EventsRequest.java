package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.dto;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.AnalyticsEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class EventsRequest {
    
    @NotEmpty(message = "Events array cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 events per request")
    @Valid
    private List<AnalyticsEvent> events;
    
    public EventsRequest() {}
    
    public EventsRequest(List<AnalyticsEvent> events) {
        this.events = events;
    }
    
    public List<AnalyticsEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<AnalyticsEvent> events) {
        this.events = events;
    }
}
