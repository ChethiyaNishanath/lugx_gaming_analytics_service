package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScrollEvent extends BaseAnalyticsEvent {

  @JsonProperty("scroll_depth")
  private Integer scrollDepth = 0;

  @JsonProperty("scroll_percentage")
  private Double scrollPercentage = 0.0;
}
