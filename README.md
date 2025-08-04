# Analytics Service - Spring Boot

This is a Spring Boot microservice for collecting and analyzing web analytics data, converted from the original Node.js implementation.

## Features

- Event collection and enrichment
- ClickHouse integration for high-performance analytics
- **AWS Redshift integration** for data warehousing
- AWS S3 export for QuickSight integration
- Rate limiting
- Real-time metrics
- Dashboard data endpoints
- User journey tracking
- Performance monitoring
- Health checks
- Dual database support (ClickHouse + Redshift)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- ClickHouse database
- AWS Account (for Redshift and S3 integration)
- AWS Redshift cluster (optional)

## Configuration

The application can be configured via environment variables or `application.properties`:

```properties
# Server
server.port=3000

# ClickHouse
clickhouse.host=localhost:8123
clickhouse.username=default
clickhouse.password=
clickhouse.database=analytics

# AWS Redshift (optional)
aws.redshift.enabled=true
aws.redshift.cluster.id=lugx-analytics-cluster
aws.redshift.database=analytics
aws.redshift.cluster.endpoint=your-cluster.region.redshift.amazonaws.com
aws.redshift.port=5439
aws.redshift.username=admin
aws.redshift.password=YourPassword123
aws.redshift.async.enabled=true
aws.redshift.batch.size=1000

# AWS Configuration
aws.access-key-id=your-access-key
aws.secret-access-key=your-secret-key
aws.region=us-east-1
aws.s3.bucket-name=your-s3-bucket
aws.s3.export.enabled=true

# CORS
cors.allowed-origins=*

# Rate Limiting
rate.limit.window-minutes=15
rate.limit.max-requests=1000
```

## Building and Running

### Using Maven

```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run

# Build JAR file
mvn clean package

# Run JAR file
java -jar target/analytics-0.0.1-SNAPSHOT.jar
```

### Using Maven Wrapper (Windows)

```cmd
# Build the application
.\mvnw.cmd clean compile

# Run the application
.\mvnw.cmd spring-boot:run

# Build JAR file
.\mvnw.cmd clean package
```

## API Endpoints

### Analytics Endpoints

- `POST /analytics/events` - Submit analytics events (all types)
- `GET /analytics/dashboard` - Get comprehensive dashboard data
- `GET /analytics/realtime` - Get real-time metrics
- `GET /analytics/user-journey/{sessionId}` - Get user journey data
- `GET /analytics/performance` - Get performance metrics

#### Specific Event Type Endpoints
- `GET /analytics/page-views` - Page view analytics
- `GET /analytics/clicks` - Click analytics  
- `GET /analytics/scrolls` - Scroll analytics
- `GET /analytics/sessions` - Session analytics

### Health Check

- `GET /health` - Application health status

## Event Data Format

The service now supports separated event types with specialized endpoints:

### Page View Events
```json
{
  "pageViews": [
    {
      "session_id": "session123",
      "page_url": "https://example.com/page",
      "page_title": "Example Page",
      "timestamp": "2025-07-28T10:00:00",
      "user_agent": "Mozilla/5.0...",
      "page_load_time": 1200,
      "time_on_page": 30000,
      "is_bounce": false,
      "entry_page": true,
      "exit_page": false
    }
  ]
}
```

### Click Events
```json
{
  "clicks": [
    {
      "session_id": "session123",
      "page_url": "https://example.com/page",
      "timestamp": "2025-07-28T10:00:30",
      "click_x": 150,
      "click_y": 200,
      "element_id": "btn-submit",
      "element_class": "btn primary",
      "element_tag": "button",
      "element_text": "Submit",
      "click_type": "left",
      "is_double_click": false
    }
  ]
}
```

### Scroll Events
```json
{
  "scrolls": [
    {
      "session_id": "session123",
      "page_url": "https://example.com/page",
      "timestamp": "2025-07-28T10:01:00",
      "scroll_depth": 500,
      "max_scroll_depth": 750,
      "scroll_percentage": 75.5,
      "scroll_direction": "down",
      "scroll_speed": 100,
      "time_to_scroll": 60000
    }
  ]
}
```

### Session Events
```json
{
  "sessions": [
    {
      "session_id": "session123",
      "event_type": "session_end",
      "timestamp": "2025-07-28T10:05:00",
      "session_duration": 300000,
      "page_count": 3,
      "total_clicks": 5,
      "total_scroll_depth": 750,
      "is_bounce": false,
      "entry_page": "https://example.com/home",
      "exit_page": "https://example.com/contact"
    }
  ]
}
```

## Valid Session Event Types

- `session_start`
- `session_end`
- `session_update`

## ClickHouse Setup

The service uses separate tables for different analytics events for better performance and organization:

### 1. Page View Events Table
```sql
CREATE TABLE page_view_events (
    event_id String,
    session_id String,
    user_id Nullable(String),
    page_url String,
    page_title String,
    timestamp DateTime,
    server_timestamp DateTime,
    user_agent String,
    country String,
    city String,
    browser String,
    browser_version String,
    os String,
    os_version String,
    device_type String,
    screen_width UInt32,
    screen_height UInt32,
    viewport_width UInt32,
    viewport_height UInt32,
    referrer String,
    page_load_time UInt32,
    time_on_page UInt32,
    is_bounce UInt8,
    entry_page UInt8,
    exit_page UInt8
) ENGINE = MergeTree()
ORDER BY (timestamp, session_id)
PARTITION BY toYYYYMM(timestamp);
```

### 2. Click Events Table
```sql
CREATE TABLE click_events (
    event_id String,
    session_id String,
    user_id Nullable(String),
    page_url String,
    timestamp DateTime,
    server_timestamp DateTime,
    user_agent String,
    country String,
    city String,
    browser String,
    browser_version String,
    os String,
    os_version String,
    device_type String,
    screen_width UInt32,
    screen_height UInt32,
    viewport_width UInt32,
    viewport_height UInt32,
    referrer String,
    click_x UInt32,
    click_y UInt32,
    element_id String,
    element_class String,
    element_tag String,
    element_text String,
    click_type String,
    is_double_click UInt8
) ENGINE = MergeTree()
ORDER BY (timestamp, session_id)
PARTITION BY toYYYYMM(timestamp);
```

### 3. Scroll Events Table
```sql
CREATE TABLE scroll_events (
    event_id String,
    session_id String,
    user_id Nullable(String),
    page_url String,
    timestamp DateTime,
    server_timestamp DateTime,
    user_agent String,
    country String,
    city String,
    browser String,
    browser_version String,
    os String,
    os_version String,
    device_type String,
    screen_width UInt32,
    screen_height UInt32,
    viewport_width UInt32,
    viewport_height UInt32,
    referrer String,
    scroll_depth UInt32,
    max_scroll_depth UInt32,
    scroll_percentage Float64,
    scroll_direction String,
    scroll_speed UInt32,
    time_to_scroll UInt32
) ENGINE = MergeTree()
ORDER BY (timestamp, session_id)
PARTITION BY toYYYYMM(timestamp);
```

### 4. Session Events Table
```sql
CREATE TABLE session_events (
    event_id String,
    session_id String,
    user_id Nullable(String),
    page_url String,
    timestamp DateTime,
    server_timestamp DateTime,
    user_agent String,
    country String,
    city String,
    browser String,
    browser_version String,
    os String,
    os_version String,
    device_type String,
    screen_width UInt32,
    screen_height UInt32,
    viewport_width UInt32,
    viewport_height UInt32,
    referrer String,
    event_type String,
    session_duration UInt32,
    page_count UInt32,
    total_clicks UInt32,
    total_scroll_depth UInt32,
    is_bounce UInt8,
    entry_page String,
    exit_page String
) ENGINE = MergeTree()
ORDER BY (timestamp, session_id)
PARTITION BY toYYYYMM(timestamp);
```

**To set up the database, run the SQL script:**
```bash
clickhouse-client --multiquery < clickhouse_schema.sql
```

## Conversion Notes from Node.js

This Spring Boot implementation provides equivalent functionality to the original Node.js service:

- Express routes → Spring MVC Controllers
- Middleware → Spring Interceptors/Filters
- ClickHouse client → JDBC connection
- Rate limiting → Bucket4j library
- User agent parsing → UserAgentUtils library
- CORS handling → Spring CORS configuration
- Error handling → Global exception handler

## Monitoring

The application includes:
- Spring Boot Actuator health endpoints
- Custom health checks for ClickHouse connectivity
- Structured logging
- Rate limiting with configurable thresholds

## Development

For development mode, you can set:
```properties
logging.level.com.bigdata.analytics=DEBUG
```

This will provide detailed logging for debugging purposes.

---

## Original Spring Boot Documentation

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.3/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.3/reference/web/servlet.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
