-- ClickHouse DDL for Analytics Service
-- Created: July 28, 2025
-- Purpose: Create separated tables for different types of analytics events

-- Database creation (REQUIRED - creates the analytics database)
CREATE DATABASE IF NOT EXISTS analytics;
USE analytics;

-- =============================================================================
-- PAGE VIEW EVENTS TABLE
-- =============================================================================
-- Stores all page view related analytics data
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

-- Indexes for page view events
CREATE INDEX IF NOT EXISTS idx_page_view_session ON page_view_events (session_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_page_view_user ON page_view_events (user_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_page_view_url ON page_view_events (page_url) TYPE bloom_filter GRANULARITY 1;

-- =============================================================================
-- CLICK EVENTS TABLE
-- =============================================================================
-- Stores all click interaction analytics data
CREATE TABLE IF NOT EXISTS click_events (
    event_id String,
    session_id String,
    user_id String,
    page_url String,
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
    click_x UInt32,
    click_y UInt32,
    element_id String,
    element_class String,
    element_tag String,
    element_text String,
    click_type String,
    is_double_click UInt8
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, session_id, user_id)
TTL timestamp + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;

-- Indexes for click events
CREATE INDEX IF NOT EXISTS idx_click_session ON click_events (session_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_click_element ON click_events (element_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_click_tag ON click_events (element_tag) TYPE bloom_filter GRANULARITY 1;

-- =============================================================================
-- SCROLL EVENTS TABLE
-- =============================================================================
-- Stores all scroll behavior analytics data
CREATE TABLE IF NOT EXISTS scroll_events (
    event_id String,
    session_id String,
    user_id String,
    page_url String,
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
    scroll_depth UInt32,
    max_scroll_depth UInt32,
    scroll_percentage Float64,
    scroll_direction String,
    scroll_speed UInt32,
    time_to_scroll UInt32
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, session_id, user_id)
TTL timestamp + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;

-- Indexes for scroll events
CREATE INDEX IF NOT EXISTS idx_scroll_session ON scroll_events (session_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_scroll_url ON scroll_events (page_url) TYPE bloom_filter GRANULARITY 1;

-- =============================================================================
-- SESSION EVENTS TABLE
-- =============================================================================
-- Stores session-level analytics data (session start/end events)
CREATE TABLE IF NOT EXISTS session_events (
    event_id String,
    session_id String,
    user_id String,
    page_url String,
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
    event_type String,
    session_duration UInt32,
    page_count UInt32,
    total_clicks UInt32,
    total_scroll_depth UInt32,
    is_bounce UInt8,
    entry_page String,
    exit_page String
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, session_id, user_id)
TTL timestamp + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;

-- Indexes for session events
CREATE INDEX IF NOT EXISTS idx_session_id ON session_events (session_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_session_type ON session_events (event_type) TYPE bloom_filter GRANULARITY 1;

-- =============================================================================
-- MATERIALIZED VIEWS FOR AGGREGATED DATA (OPTIONAL)
-- =============================================================================

-- Real-time hourly page view aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS page_views_hourly_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (hour, page_url)
AS SELECT
    toStartOfHour(timestamp) as hour,
    page_url,
    page_title,
    count() as views,
    uniq(session_id) as unique_sessions,
    uniq(user_id) as unique_users,
    avg(page_load_time) as avg_load_time,
    countIf(is_bounce = 1) as bounces
FROM page_view_events
GROUP BY hour, page_url, page_title;

-- Daily session aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS sessions_daily_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(day)
ORDER BY (day, country, device_type)
AS SELECT
    toStartOfDay(timestamp) as day,
    country,
    device_type,
    browser,
    count() as sessions,
    uniq(user_id) as unique_users,
    avg(session_duration) as avg_duration,
    countIf(is_bounce = 1) as bounces
FROM session_events
WHERE event_type = 'session_end'
GROUP BY day, country, device_type, browser;

-- =============================================================================
-- USEFUL QUERIES FOR TESTING
-- =============================================================================

-- Test table existence
-- SELECT name FROM system.tables WHERE database = currentDatabase() AND name LIKE '%events';

-- Check table sizes
-- SELECT
--     name,
--     formatReadableSize(total_bytes) as size,
--     rows
-- FROM system.tables
-- WHERE database = currentDatabase() AND name LIKE '%events'
-- ORDER BY total_bytes DESC;

-- Sample data queries (use after inserting data)
-- SELECT count() FROM page_view_events;
-- SELECT count() FROM click_events;
-- SELECT count() FROM scroll_events;
-- SELECT count() FROM session_events;

-- =============================================================================
-- CLEANUP COMMANDS (USE WITH CAUTION)
-- =============================================================================

-- Drop all tables (uncomment if needed for cleanup)
-- DROP TABLE IF EXISTS page_view_events;
-- DROP TABLE IF EXISTS click_events;
-- DROP TABLE IF EXISTS scroll_events;
-- DROP TABLE IF EXISTS session_events;
-- DROP VIEW IF EXISTS page_views_hourly_mv;
-- DROP VIEW IF EXISTS sessions_daily_mv;

-- =============================================================================
-- PERFORMANCE OPTIMIZATION SETTINGS
-- =============================================================================

-- Optimize table settings for better performance
-- ALTER TABLE page_view_events MODIFY SETTING merge_with_ttl_timeout = 3600;
-- ALTER TABLE click_events MODIFY SETTING merge_with_ttl_timeout = 3600;
-- ALTER TABLE scroll_events MODIFY SETTING merge_with_ttl_timeout = 3600;
-- ALTER TABLE session_events MODIFY SETTING merge_with_ttl_timeout = 3600;

-- =============================================================================
-- NOTES
-- =============================================================================
-- 1. TTL is set to 90 days - adjust based on your data retention requirements
-- 2. Partitioning by month (toYYYYMM) provides good balance for most use cases
-- 3. Index granularity of 8192 is the default and works well for most scenarios
-- 4. Bloom filter indexes help with string filtering performance
-- 5. Materialized views provide pre-aggregated data for common queries
-- 6. Adjust data types and sizes based on your specific requirements
