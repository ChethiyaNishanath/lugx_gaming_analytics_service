-- Test Script for Redshift Integration
-- Run this script to verify that tables are created correctly and data can be inserted

-- Test 1: Verify tables exist
SELECT schemaname, tablename, tableowner 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('page_view_events', 'click_events', 'scroll_events', 'session_events');

-- Test 2: Check table structure
\d public.page_view_events
\d public.click_events
\d public.scroll_events
\d public.session_events

-- Test 3: Insert sample data
BEGIN;

-- Insert test page view event
INSERT INTO public.page_view_events (
    session_id, user_id, page_url, page_title, referrer, load_time,
    timestamp, user_agent, ip_address, device_type, browser, os, country, city
) VALUES (
    'test-session-001',
    'test-user-001', 
    'https://lugx-gaming.com/home',
    'LUGX Gaming - Home',
    'https://google.com',
    1250,
    CURRENT_TIMESTAMP,
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    '192.168.1.100',
    'desktop',
    'Chrome',
    'Windows',
    'United States',
    'New York'
);

-- Insert test click event
INSERT INTO public.click_events (
    session_id, user_id, element_id, element_text, page_url, click_x, click_y,
    timestamp, user_agent, ip_address, device_type, browser, os, country, city
) VALUES (
    'test-session-001',
    'test-user-001',
    'btn-games',
    'View Games',
    'https://lugx-gaming.com/home',
    350,
    200,
    CURRENT_TIMESTAMP,
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    '192.168.1.100',
    'desktop',
    'Chrome',
    'Windows',
    'United States',
    'New York'
);

-- Insert test scroll event
INSERT INTO public.scroll_events (
    session_id, user_id, page_url, scroll_depth, scroll_percentage,
    timestamp, user_agent, ip_address, device_type, browser, os, country, city
) VALUES (
    'test-session-001',
    'test-user-001',
    'https://lugx-gaming.com/home',
    1200,
    75.50,
    CURRENT_TIMESTAMP,
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    '192.168.1.100',
    'desktop',
    'Chrome',
    'Windows',
    'United States',
    'New York'
);

-- Insert test session event
INSERT INTO public.session_events (
    session_id, user_id, event_type, page_count,
    timestamp, user_agent, ip_address, device_type, browser, os, country, city
) VALUES (
    'test-session-001',
    'test-user-001',
    'session_start',
    1,
    CURRENT_TIMESTAMP,
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    '192.168.1.100',
    'desktop',
    'Chrome',
    'Windows',
    'United States',
    'New York'
);

COMMIT;

-- Test 4: Verify data was inserted
SELECT 'Page View Events' as table_name, COUNT(*) as record_count FROM public.page_view_events WHERE session_id = 'test-session-001'
UNION ALL
SELECT 'Click Events' as table_name, COUNT(*) as record_count FROM public.click_events WHERE session_id = 'test-session-001'
UNION ALL
SELECT 'Scroll Events' as table_name, COUNT(*) as record_count FROM public.scroll_events WHERE session_id = 'test-session-001'
UNION ALL
SELECT 'Session Events' as table_name, COUNT(*) as record_count FROM public.session_events WHERE session_id = 'test-session-001';

-- Test 5: Query sample data
SELECT 'Latest Page Views' as query_type, session_id, user_id, page_url, timestamp 
FROM public.page_view_events 
ORDER BY timestamp DESC 
LIMIT 5;

SELECT 'Latest Clicks' as query_type, session_id, element_id, element_text, page_url, timestamp 
FROM public.click_events 
ORDER BY timestamp DESC 
LIMIT 5;

-- Test 6: Test analytics views
SELECT * FROM public.analytics_summary ORDER BY hour DESC LIMIT 10;
SELECT * FROM public.top_pages LIMIT 10;
SELECT * FROM public.click_heatmap LIMIT 10;

-- Test 7: Performance test (optional - uncomment to run)
/*
-- Generate more test data for performance testing
INSERT INTO public.page_view_events (
    session_id, user_id, page_url, page_title, timestamp, device_type, browser, country
)
SELECT 
    'perf-test-' || generate_series(1, 1000)::text,
    'user-' || (random() * 100)::int::text,
    'https://lugx-gaming.com/page-' || (random() * 50)::int::text,
    'Test Page ' || (random() * 50)::int::text,
    CURRENT_TIMESTAMP - (random() * interval '30 days'),
    CASE (random() * 3)::int 
        WHEN 0 THEN 'desktop' 
        WHEN 1 THEN 'mobile' 
        ELSE 'tablet' 
    END,
    CASE (random() * 3)::int 
        WHEN 0 THEN 'Chrome' 
        WHEN 1 THEN 'Firefox' 
        ELSE 'Safari' 
    END,
    CASE (random() * 5)::int 
        WHEN 0 THEN 'United States' 
        WHEN 1 THEN 'Canada' 
        WHEN 2 THEN 'United Kingdom'
        WHEN 3 THEN 'Germany'
        ELSE 'France'
    END;
*/

-- Test 8: Check table sizes and storage
SELECT 
    schemaname,
    tablename,
    attname as column_name,
    n_distinct,
    correlation
FROM pg_stats 
WHERE schemaname = 'public' 
AND tablename IN ('page_view_events', 'click_events', 'scroll_events', 'session_events')
ORDER BY schemaname, tablename, attname;

-- Test 9: Validate constraints and indexes
SELECT 
    i.relname as index_name,
    t.relname as table_name,
    array_to_string(array_agg(a.attname), ', ') as column_names
FROM 
    pg_class t,
    pg_class i,
    pg_index ix,
    pg_attribute a
WHERE 
    t.oid = ix.indrelid
    AND i.oid = ix.indexrelid
    AND a.attrelid = t.oid
    AND a.attnum = ANY(ix.indkey)
    AND t.relkind = 'r'
    AND t.relname IN ('page_view_events', 'click_events', 'scroll_events', 'session_events')
GROUP BY i.relname, t.relname
ORDER BY t.relname, i.relname;

-- Test 10: Connection and performance info
SELECT 
    CURRENT_DATABASE() as current_database,
    CURRENT_USER as current_user,
    CURRENT_TIMESTAMP as current_time,
    VERSION() as redshift_version;

-- Clean up test data (optional - uncomment to clean up)
/*
DELETE FROM public.page_view_events WHERE session_id LIKE 'test-session-%' OR session_id LIKE 'perf-test-%';
DELETE FROM public.click_events WHERE session_id LIKE 'test-session-%' OR session_id LIKE 'perf-test-%';
DELETE FROM public.scroll_events WHERE session_id LIKE 'test-session-%' OR session_id LIKE 'perf-test-%';
DELETE FROM public.session_events WHERE session_id LIKE 'test-session-%' OR session_id LIKE 'perf-test-%';
*/

-- Success message
SELECT 'Redshift integration test completed successfully!' as status;
