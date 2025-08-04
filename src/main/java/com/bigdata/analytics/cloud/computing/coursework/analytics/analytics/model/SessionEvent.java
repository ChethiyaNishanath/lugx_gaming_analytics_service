package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SessionEvent extends BaseAnalyticsEvent {

  @JsonProperty("event_type")
  private String eventType;

  @JsonProperty("duration")
  private Integer duration = 0;

  @JsonProperty("page_count")
  private Integer pageCount = 1;
}
