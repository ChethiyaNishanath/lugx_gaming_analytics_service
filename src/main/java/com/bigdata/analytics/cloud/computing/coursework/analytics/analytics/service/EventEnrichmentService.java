package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service;

import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.BaseAnalyticsEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ClickEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.PageViewEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.ScrollEvent;
import com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.model.SessionEvent;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class EventEnrichmentService {

  public PageViewEvent enrichPageViewEvent(PageViewEvent event, HttpServletRequest request) {
    enrichBaseEvent(event, request);

    if (event.getPageTitle() == null) event.setPageTitle("");

    return event;
  }

  public ClickEvent enrichClickEvent(ClickEvent event, HttpServletRequest request) {
    enrichBaseEvent(event, request);

    if (event.getClickX() == null) event.setClickX(0);
    if (event.getClickY() == null) event.setClickY(0);
    if (event.getElementId() == null) event.setElementId("");
    if (event.getElementText() == null) event.setElementText("");

    return event;
  }

  public ScrollEvent enrichScrollEvent(ScrollEvent event, HttpServletRequest request) {
    enrichBaseEvent(event, request);

    if (event.getScrollDepth() == null) event.setScrollDepth(0);
    if (event.getScrollPercentage() == null) event.setScrollPercentage(0.0);

    return event;
  }

  public SessionEvent enrichSessionEvent(SessionEvent event, HttpServletRequest request) {
    enrichBaseEvent(event, request);

    if (event.getEventType() == null) event.setEventType("session_start");
    if (event.getPageCount() == null) event.setPageCount(1);

    return event;
  }

  private void enrichBaseEvent(BaseAnalyticsEvent event, HttpServletRequest request) {

    if (event.getTimestamp() == null || event.getTimestamp().isEmpty()) {
      LocalDateTime parsedTimestamp = LocalDateTime.now(); // assuming ISO format
      String formatted = parsedTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      // event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      event.setTimestamp(formatted);
    }

    String userAgentString = event.getUserAgent();
    if (userAgentString == null || userAgentString.isEmpty()) {
      userAgentString = request.getHeader("User-Agent");
      event.setUserAgent(userAgentString);
    }

    if (userAgentString != null) {
      UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

      if (event.getBrowser() == null || event.getBrowser().isEmpty()) {
        event.setBrowser(userAgent.getBrowser().getName());
      }

      if (event.getOs() == null || event.getOs().isEmpty()) {
        event.setOs(userAgent.getOperatingSystem().getName());
      }

      if (event.getDeviceType() == null || event.getDeviceType().isEmpty()) {
        event.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
      }
    }

    String clientIP = getClientIP(request);
    if (event.getCountry() == null || event.getCountry().isEmpty()) {
      event.setCountry("Unknown");
    }
    if (event.getCity() == null || event.getCity().isEmpty()) {
      event.setCity("Unknown");
    }

    ensureBaseFieldsNotNull(event);
  }

  private String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIP = request.getHeader("X-Real-IP");
    if (xRealIP != null && !xRealIP.isEmpty()) {
      return xRealIP;
    }

    return request.getRemoteAddr();
  }

  private void ensureBaseFieldsNotNull(BaseAnalyticsEvent event) {
    if (event.getUserAgent() == null) event.setUserAgent("");
    if (event.getBrowser() == null) event.setBrowser("Unknown");
    if (event.getOs() == null) event.setOs("Unknown");
    if (event.getDeviceType() == null) event.setDeviceType("Unknown");
    if (event.getCountry() == null) event.setCountry("Unknown");
    if (event.getCity() == null) event.setCity("Unknown");
  }
}
