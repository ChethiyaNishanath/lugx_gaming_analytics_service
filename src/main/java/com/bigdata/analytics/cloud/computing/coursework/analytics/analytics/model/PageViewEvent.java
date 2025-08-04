package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageViewEvent extends BaseAnalyticsEvent {
    
    @JsonProperty("page_title")
    private String pageTitle;
    
    @JsonProperty("referrer")
    private String referrer;
    
    @JsonProperty("load_time")
    private Integer loadTime = 0;
}
