# Frontend Analytics Integration

This directory contains a simplified analytics SDK for frontend integration with your Spring Boot analytics service.

## Files

- `analytics-sdk.js` - The main analytics SDK for tracking events
- `demo.html` - A demonstration page showing how to use the SDK

## Simplified Event Models

The analytics service has been simplified to focus on essential attributes:

### Base Event Fields (Common to all events)
- `session_id` - Unique session identifier
- `user_id` - User identifier (optional)
- `page_url` - Current page URL
- `timestamp` - Event timestamp
- `user_agent` - Browser user agent
- `ip_address` - User IP address
- `device_type` - Device type (mobile, desktop, tablet)
- `browser` - Browser name
- `os` - Operating system
- `country` - User country
- `city` - User city

### Event Types

#### 1. Page View Events
- `page_title` - Page title
- `referrer` - Referring page
- `load_time` - Page load time in milliseconds

#### 2. Click Events
- `element_id` - Clicked element ID
- `element_text` - Clicked element text
- `click_x` - X coordinate of click
- `click_y` - Y coordinate of click

#### 3. Scroll Events
- `scroll_depth` - Scroll position in pixels
- `scroll_percentage` - Scroll percentage (0-100)

#### 4. Session Events
- `event_type` - Type of session event (session_start, session_end, session_update)
- `duration` - Session duration in seconds
- `page_count` - Number of pages visited in session

## Quick Start

1. **Include the SDK in your HTML:**
```html
<script src="analytics-sdk.js"></script>
```

2. **Initialize the SDK:**
```javascript
Analytics.init({
    apiUrl: 'http://localhost:3000/analytics/events'
});
```

3. **Track events manually (optional):**
```javascript
// Track a custom page view
Analytics.trackPageView({ custom_property: 'value' });

// Track a click event
Analytics.trackClick(element, { additional_data: 'value' });

// Track scroll events
Analytics.trackScroll({ custom_data: 'value' });

// Track session events
Analytics.trackSession('session_start');
```

## Automatic Tracking

The SDK automatically tracks:
- Page views when initialized
- Clicks on buttons, links, and elements with onclick handlers
- Scroll events (throttled to every 500ms)
- Session start and end events

## Configuration

### Backend Configuration
Make sure your Spring Boot application is running on the correct port and has CORS enabled for your frontend domain.

In `application.properties`:
```properties
# Server configuration
server.port=3000

# CORS configuration
cors.allowed-origins=http://localhost:8080,http://127.0.0.1:8080

# S3 Export (can be disabled for testing)
aws.s3.export.enabled=true
```

### Frontend Configuration
Update the `apiUrl` in the initialization to match your backend:
```javascript
Analytics.init({
    apiUrl: 'http://your-backend-url:3000/analytics/events'
});
```

## Testing

1. Start your Spring Boot analytics service
2. Open `demo.html` in a web browser
3. Test different events using the demo interface
4. Check the browser console and server logs for confirmation

## API Format

The SDK sends data in this format:
```json
{
  "page_views": [
    {
      "session_id": "session_12345",
      "user_id": "user_67890",
      "page_url": "https://example.com/page",
      "page_title": "Example Page",
      "referrer": "https://google.com",
      "load_time": 1500,
      "timestamp": "2025-01-01T12:00:00.000Z"
    }
  ],
  "clicks": [...],
  "scrolls": [...],
  "sessions": [...]
}
```

## Features

- ✅ Simplified event models for easy integration
- ✅ Automatic event tracking
- ✅ Manual event tracking
- ✅ Session management
- ✅ User identification
- ✅ Async S3 export to AWS (for QuickSight)
- ✅ Error handling and logging
- ✅ CORS support
- ✅ Rate limiting protection

## Next Steps

1. Customize the SDK for your specific needs
2. Add more event types if required
3. Implement user authentication integration
4. Set up AWS QuickSight dashboards using the S3 exported data
5. Monitor analytics data in ClickHouse database
