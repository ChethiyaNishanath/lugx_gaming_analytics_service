-- Redshift DDL Script for Analytics Tables
-- Run this script to create the required tables in your Redshift cluster

-- Create page_view_events table
CREATE TABLE IF NOT EXISTS public.page_view_events (
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    page_url VARCHAR(2048) NOT NULL,
    page_title VARCHAR(1024),
    referrer VARCHAR(2048),
    load_time INTEGER,
    timestamp TIMESTAMP NOT NULL,
    user_agent VARCHAR(1024),
    ip_address VARCHAR(45),
    device_type VARCHAR(50),
    browser VARCHAR(100),
    os VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT GETDATE()
)
DISTSTYLE KEY
DISTKEY (session_id)
SORTKEY (timestamp, session_id);

-- Create click_events table
CREATE TABLE IF NOT EXISTS public.click_events (
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    element_id VARCHAR(255),
    element_text VARCHAR(1024),
    page_url VARCHAR(2048) NOT NULL,
    click_x INTEGER,
    click_y INTEGER,
    timestamp TIMESTAMP NOT NULL,
    user_agent VARCHAR(1024),
    ip_address VARCHAR(45),
    device_type VARCHAR(50),
    browser VARCHAR(100),
    os VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT GETDATE()
)
DISTSTYLE KEY
DISTKEY (session_id)
SORTKEY (timestamp, session_id);

-- Create scroll_events table
CREATE TABLE IF NOT EXISTS public.scroll_events (
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    page_url VARCHAR(2048) NOT NULL,
    scroll_depth INTEGER,
    scroll_percentage DECIMAL(5,2),
    timestamp TIMESTAMP NOT NULL,
    user_agent VARCHAR(1024),
    ip_address VARCHAR(45),
    device_type VARCHAR(50),
    browser VARCHAR(100),
    os VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT GETDATE()
)
DISTSTYLE KEY
DISTKEY (session_id)
SORTKEY (timestamp, session_id);

-- Create session_events table
CREATE TABLE IF NOT EXISTS public.session_events (
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    event_type VARCHAR(50) NOT NULL,
    page_count INTEGER,
    timestamp TIMESTAMP NOT NULL,
    user_agent VARCHAR(1024),
    ip_address VARCHAR(45),
    device_type VARCHAR(50),
    browser VARCHAR(100),
    os VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT GETDATE()
)
DISTSTYLE KEY
DISTKEY (session_id)
SORTKEY (timestamp, session_id);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_page_view_events_timestamp ON public.page_view_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_page_view_events_session_id ON public.page_view_events(session_id);
CREATE INDEX IF NOT EXISTS idx_page_view_events_user_id ON public.page_view_events(user_id);
CREATE INDEX IF NOT EXISTS idx_page_view_events_page_url ON public.page_view_events(page_url);

CREATE INDEX IF NOT EXISTS idx_click_events_timestamp ON public.click_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_click_events_session_id ON public.click_events(session_id);
CREATE INDEX IF NOT EXISTS idx_click_events_user_id ON public.click_events(user_id);
CREATE INDEX IF NOT EXISTS idx_click_events_page_url ON public.click_events(page_url);

CREATE INDEX IF NOT EXISTS idx_scroll_events_timestamp ON public.scroll_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_scroll_events_session_id ON public.scroll_events(session_id);
CREATE INDEX IF NOT EXISTS idx_scroll_events_user_id ON public.scroll_events(user_id);
CREATE INDEX IF NOT EXISTS idx_scroll_events_page_url ON public.scroll_events(page_url);

CREATE INDEX IF NOT EXISTS idx_session_events_timestamp ON public.session_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_session_events_session_id ON public.session_events(session_id);
CREATE INDEX IF NOT EXISTS idx_session_events_user_id ON public.session_events(user_id);
CREATE INDEX IF NOT EXISTS idx_session_events_event_type ON public.session_events(event_type);

-- Grant permissions (adjust as needed for your setup)
GRANT SELECT, INSERT, UPDATE, DELETE ON public.page_view_events TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.click_events TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.scroll_events TO public;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.session_events TO public;

-- Create views for analytics queries
CREATE OR REPLACE VIEW public.analytics_summary AS
SELECT 
    DATE_TRUNC('hour', timestamp) as hour,
    COUNT(*) as total_page_views,
    COUNT(DISTINCT session_id) as unique_sessions,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(load_time) as avg_load_time
FROM public.page_view_events
WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY hour
ORDER BY hour;

CREATE OR REPLACE VIEW public.top_pages AS
SELECT 
    page_url,
    page_title,
    COUNT(*) as views,
    COUNT(DISTINCT session_id) as unique_sessions,
    AVG(load_time) as avg_load_time
FROM public.page_view_events
WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY page_url, page_title
ORDER BY views DESC
LIMIT 50;

CREATE OR REPLACE VIEW public.click_heatmap AS
SELECT 
    page_url,
    element_id,
    element_text,
    COUNT(*) as clicks,
    COUNT(DISTINCT session_id) as unique_sessions,
    AVG(click_x) as avg_x,
    AVG(click_y) as avg_y
FROM public.click_events
WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days'
    AND click_x IS NOT NULL 
    AND click_y IS NOT NULL
GROUP BY page_url, element_id, element_text
HAVING COUNT(*) > 5
ORDER BY clicks DESC;

-- Create materialized views for faster analytics
CREATE MATERIALIZED VIEW public.daily_analytics AS
SELECT 
    DATE_TRUNC('day', timestamp) as day,
    COUNT(*) as total_page_views,
    COUNT(DISTINCT session_id) as unique_sessions,
    COUNT(DISTINCT user_id) as unique_users,
    AVG(load_time) as avg_load_time,
    COUNT(DISTINCT page_url) as unique_pages
FROM public.page_view_events
GROUP BY day
ORDER BY day;

-- Refresh the materialized view
REFRESH MATERIALIZED VIEW public.daily_analytics;
