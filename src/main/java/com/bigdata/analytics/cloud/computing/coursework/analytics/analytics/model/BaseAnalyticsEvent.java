package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaseAnalyticsEvent {

  @NotBlank(message = "Session ID is required")
  @JsonProperty("session_id")
  private String sessionId;

  @JsonProperty("user_id")
  private String userId;

  @NotBlank(message = "Page URL is required")
  @JsonProperty("page_url")
  private String pageUrl;

  private String timestamp;

  @JsonProperty("user_agent")
  private String userAgent;

  @JsonProperty("ip_address")
  private String ipAddress;

  @JsonProperty("device_type")
  private String deviceType;
  
  private String browser;
  private String os;
  private String country;
  private String city;
}
