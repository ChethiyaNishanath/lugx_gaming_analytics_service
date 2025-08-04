-- Quick setup script for analytics database
-- Run this in your ClickHouse client to create the analytics database and tables

-- Step 1: Create the analytics database
CREATE DATABASE IF NOT EXISTS analytics;

-- Step 2: Use the analytics database
USE analytics;

-- Step 3: Create the four main tables
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

-- Verify tables were created
SHOW TABLES IN analytics;
