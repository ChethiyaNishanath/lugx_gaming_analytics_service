package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClickEvent extends BaseAnalyticsEvent {

  @JsonProperty("element_id")
  private String elementId;

  @JsonProperty("element_text")
  private String elementText;
  
  @JsonProperty("click_x")
  private Integer clickX = 0;

  @JsonProperty("click_y")
  private Integer clickY = 0;
}
