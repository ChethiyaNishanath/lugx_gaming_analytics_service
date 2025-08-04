-- ClickHouse Database Schema for Separated Analytics Tables
-- Run these commands to create the required tables for the analytics service

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS analytics;

-- Use the analytics database
USE analytics;

-- 1. Page View Events Table
CREATE TABLE IF NOT EXISTS page_view_events (
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

-- 2. Click Events Table
CREATE TABLE IF NOT EXISTS click_events (
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

-- 3. Scroll Events Table
CREATE TABLE IF NOT EXISTS scroll_events (
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

-- 4. Session Events Table
CREATE TABLE IF NOT EXISTS session_events (
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

-- Create indexes for better query performance
-- Page View Events indexes
ALTER TABLE page_view_events ADD INDEX idx_page_url page_url TYPE bloom_filter GRANULARITY 1;
ALTER TABLE page_view_events ADD INDEX idx_user_id user_id TYPE bloom_filter GRANULARITY 1;

-- Click Events indexes
ALTER TABLE click_events ADD INDEX idx_element_tag element_tag TYPE bloom_filter GRANULARITY 1;
ALTER TABLE click_events ADD INDEX idx_page_url page_url TYPE bloom_filter GRANULARITY 1;

-- Scroll Events indexes
ALTER TABLE scroll_events ADD INDEX idx_page_url page_url TYPE bloom_filter GRANULARITY 1;

-- Session Events indexes
ALTER TABLE session_events ADD INDEX idx_event_type event_type TYPE bloom_filter GRANULARITY 1;
ALTER TABLE session_events ADD INDEX idx_user_id user_id TYPE bloom_filter GRANULARITY 1;

-- Example queries to verify the tables work correctly:

/*
-- Insert sample page view event
INSERT INTO page_view_events VALUES (
    'pv-123', 'session-456', 'user-789', 'https://example.com/home', 'Home Page',
    '2025-07-28 10:00:00', '2025-07-28 10:00:01', 'Mozilla/5.0...',
    'US', 'New York', 'Chrome', '91.0', 'Windows', '10', 'Desktop',
    1920, 1080, 1200, 800, 'https://google.com',
    1500, 30000, 0, 1, 0
);

-- Insert sample click event
INSERT INTO click_events VALUES (
    'c-124', 'session-456', 'user-789', 'https://example.com/home',
    '2025-07-28 10:00:30', '2025-07-28 10:00:31', 'Mozilla/5.0...',
    'US', 'New York', 'Chrome', '91.0', 'Windows', '10', 'Desktop',
    1920, 1080, 1200, 800, 'https://google.com',
    150, 200, 'btn-submit', 'btn primary', 'button', 'Submit', 'left', 0
);

-- Insert sample scroll event
INSERT INTO scroll_events VALUES (
    's-125', 'session-456', 'user-789', 'https://example.com/home',
    '2025-07-28 10:01:00', '2025-07-28 10:01:01', 'Mozilla/5.0...',
    'US', 'New York', 'Chrome', '91.0', 'Windows', '10', 'Desktop',
    1920, 1080, 1200, 800, 'https://google.com',
    500, 750, 75.5, 'down', 100, 60000
);

-- Insert sample session event
INSERT INTO session_events VALUES (
    'se-126', 'session-456', 'user-789', 'https://example.com/home',
    '2025-07-28 10:05:00', '2025-07-28 10:05:01', 'Mozilla/5.0...',
    'US', 'New York', 'Chrome', '91.0', 'Windows', '10', 'Desktop',
    1920, 1080, 1200, 800, 'https://google.com',
    'session_end', 300000, 3, 5, 750, 0, 'https://example.com/home', 'https://example.com/contact'
);

-- Test queries
SELECT count() FROM page_view_events;
SELECT count() FROM click_events;
SELECT count() FROM scroll_events;
SELECT count() FROM session_events;
*/
