-- Alternative approach: If your Spring Boot app connects to the analytics database by default,
-- you can remove the "analytics." prefixes from your SQL queries.
-- 
-- Try this approach if the database connection is still pointing to 'default':
-- 
-- 1. First, verify you're connected to the right database by calling:
--    GET http://localhost:3000/analytics/database-info
--
-- 2. If it shows "default" instead of "analytics", try these approaches:

-- APPROACH 1: Use these queries WITHOUT database prefix (if already connected to analytics db)
SELECT count() FROM page_view_events;
SELECT count() FROM click_events;
SELECT count() FROM scroll_events;
SELECT count() FROM session_events;

-- APPROACH 2: Create tables in default database (if you can't change connection)
USE default;

CREATE TABLE IF NOT EXISTS page_view_events (
    event_id String,
    session_id String,
    user_id String,
    page_url String,
    page_title String,
    timestamp DateTime64(3),
    server_timestamp DateTime64(3),
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
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, session_id, user_id)
TTL timestamp + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;

-- (Add other tables similarly...)

-- APPROACH 3: Force database switch in connection
-- Update your application.properties:
-- clickhouse.url=jdbc:ch://localhost:8123/analytics?compress=0&decompress=0&use_server_time_zone=true

-- APPROACH 4: Use explicit USE statement
-- Add this to your ClickHouseConfig.java after connection:
-- connection.prepareStatement("USE analytics").execute();
