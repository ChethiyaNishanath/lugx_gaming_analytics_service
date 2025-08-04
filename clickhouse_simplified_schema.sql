-- ClickHouse Database Schema for Simplified Analytics Tables
-- Updated schema aligned with simplified event models
-- Run these commands to create the required tables for the analytics service

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS analytics;

-- Use the analytics database
USE analytics;

-- 1. Page View Events Table (Simplified)
CREATE TABLE IF NOT EXISTS page_view_events (
    session_id String,
    user_id Nullable(String),
    page_url String,
    page_title Nullable(String),
    referrer Nullable(String),
    load_time Nullable(UInt32),
    timestamp String,
    user_agent Nullable(String),
    ip_address Nullable(String),
    device_type Nullable(String),
    browser Nullable(String),
    os Nullable(String),
    country Nullable(String),
    city Nullable(String)
) ENGINE = MergeTree()
ORDER BY (session_id, timestamp)
PARTITION BY toYYYYMM(toDateTime(timestamp));

-- 2. Click Events Table (Simplified)
CREATE TABLE IF NOT EXISTS click_events (
    session_id String,
    user_id Nullable(String),
    element_id Nullable(String),
    element_text Nullable(String),
    page_url String,
    click_x Nullable(UInt32),
    click_y Nullable(UInt32),
    timestamp String,
    user_agent Nullable(String),
    ip_address Nullable(String),
    device_type Nullable(String),
    browser Nullable(String),
    os Nullable(String),
    country Nullable(String),
    city Nullable(String)
) ENGINE = MergeTree()
ORDER BY (session_id, timestamp)
PARTITION BY toYYYYMM(toDateTime(timestamp));

-- 3. Scroll Events Table (Simplified)
CREATE TABLE IF NOT EXISTS scroll_events (
    session_id String,
    user_id Nullable(String),
    page_url String,
    scroll_depth Nullable(UInt32),
    scroll_percentage Nullable(Float32),
    timestamp String,
    user_agent Nullable(String),
    ip_address Nullable(String),
    device_type Nullable(String),
    browser Nullable(String),
    os Nullable(String),
    country Nullable(String),
    city Nullable(String)
) ENGINE = MergeTree()
ORDER BY (session_id, timestamp)
PARTITION BY toYYYYMM(toDateTime(timestamp));

-- 4. Session Events Table (Simplified)
CREATE TABLE IF NOT EXISTS session_events (
    session_id String,
    user_id Nullable(String),
    event_type String,
    page_count Nullable(UInt32),
    session_duration Nullable(UInt32),
    timestamp String,
    user_agent Nullable(String),
    ip_address Nullable(String),
    device_type Nullable(String),
    browser Nullable(String),
    os Nullable(String),
    country Nullable(String),
    city Nullable(String)
) ENGINE = MergeTree()
ORDER BY (session_id, timestamp)
PARTITION BY toYYYYMM(toDateTime(timestamp));

-- Create indexes for better query performance
-- Indexes on commonly queried fields
CREATE INDEX IF NOT EXISTS idx_page_view_user_id ON page_view_events (user_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_page_view_page_url ON page_view_events (page_url) TYPE bloom_filter GRANULARITY 1;

CREATE INDEX IF NOT EXISTS idx_click_user_id ON click_events (user_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_click_page_url ON click_events (page_url) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_click_element_id ON click_events (element_id) TYPE bloom_filter GRANULARITY 1;

CREATE INDEX IF NOT EXISTS idx_scroll_user_id ON scroll_events (user_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_scroll_page_url ON scroll_events (page_url) TYPE bloom_filter GRANULARITY 1;

CREATE INDEX IF NOT EXISTS idx_session_user_id ON session_events (user_id) TYPE bloom_filter GRANULARITY 1;
CREATE INDEX IF NOT EXISTS idx_session_event_type ON session_events (event_type) TYPE bloom_filter GRANULARITY 1;

-- Views for common analytics queries
CREATE VIEW IF NOT EXISTS analytics.realtime_metrics AS
SELECT
    countIf(timestamp >= toString(now() - INTERVAL 1 HOUR)) as page_views_last_hour,
    (SELECT count() FROM analytics.click_events WHERE timestamp >= toString(now() - INTERVAL 1 HOUR)) as clicks_last_hour,
    (SELECT count() FROM analytics.scroll_events WHERE timestamp >= toString(now() - INTERVAL 1 HOUR)) as scroll_events_last_hour,
    uniqIf(session_id, timestamp >= toString(now() - INTERVAL 1 HOUR)) as active_sessions,
    uniqIf(user_id, timestamp >= toString(now() - INTERVAL 1 HOUR)) as active_users
FROM analytics.page_view_events;

-- Performance metrics view
CREATE VIEW IF NOT EXISTS analytics.performance_metrics AS
SELECT
    page_url,
    avg(load_time) as avg_load_time,
    quantile(0.5)(load_time) as median_load_time,
    quantile(0.95)(load_time) as p95_load_time,
    count() as samples
FROM analytics.page_view_events
WHERE load_time > 0
    AND timestamp >= toString(now() - INTERVAL 24 HOUR)
GROUP BY page_url
HAVING samples >= 10
ORDER BY avg_load_time DESC;
